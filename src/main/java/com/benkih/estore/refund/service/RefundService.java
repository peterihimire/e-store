package com.benkih.estore.refund.service;


import com.benkih.estore.audit.service.ApiLogService;
import com.benkih.estore.common.enums.OrderStatus;
import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.common.enums.RefundGatewayStatus;
import com.benkih.estore.common.enums.RefundStatus;
import com.benkih.estore.common.exceptions.ForbiddenException;
import com.benkih.estore.common.exceptions.PaymentException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.notification.INotificationService;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import com.benkih.estore.order.repository.OrderItemRepository;
import com.benkih.estore.order.repository.OrderRepository;
import com.benkih.estore.payment.dto.request.RefundPaymentRequest;
import com.benkih.estore.payment.dto.response.RefundPaymentResponse;
import com.benkih.estore.payment.dto.webhook.PaymentWebhookEvent;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.payment.provider.PaymentGateway;
import com.benkih.estore.payment.provider.PaymentGatewayFactory;
import com.benkih.estore.payment.repository.PaymentRepository;
import com.benkih.estore.refund.dto.request.CreateRefundRequest;
import com.benkih.estore.refund.dto.request.RefundItemRequest;
import com.benkih.estore.refund.dto.response.RefundItemResponse;
import com.benkih.estore.refund.dto.response.RefundResponse;
import com.benkih.estore.refund.entity.Refund;
import com.benkih.estore.refund.entity.RefundItem;
import com.benkih.estore.refund.repository.RefundItemRepository;
import com.benkih.estore.refund.repository.RefundRepository;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService implements IRefundService{
  private final CurrentUserService currentUserService;
  private final ApiLogService apiLogService;
  private final INotificationService notificationService;
  private final PaymentGatewayFactory gatewayFactory;
  private final RefundRepository refundRepository;
  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final RefundItemRepository refundItemRepository;


  @Override
  @Transactional
  public RefundResponse requestRefund(CreateRefundRequest request) {

    User user = currentUserService.getCurrentUser();
    Order order = orderRepository.findBySlug(request.getOrderSlug())
        .orElseThrow(() ->
            new ResourceNotFoundException("Order not found"));

    if (!order.getUser().getId().equals(user.getId())) {
      throw new ForbiddenException("You are not allowed to request a refund for this order.");
    }

    Payment payment = paymentRepository.findByOrderAndPaymentStatus(order, PaymentStatus.SUCCESS)
        .orElseThrow(() ->
            new PaymentException("This order does not have a successful payment."));

    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw new PaymentException("At least one refund item is required.");
    }

    // Prevent the same OrderItem appearing twice
    Set<String> orderItemSlugs = new HashSet<>();

    // Always acquire locks in a deterministic order
    List<RefundItemRequest> items = request.getItems()
        .stream()
        .sorted(Comparator.comparing(RefundItemRequest::getOrderItemSlug))
        .toList();

    BigDecimal totalRefundAmount = BigDecimal.ZERO;

    List<RefundItem> refundItems = new ArrayList<>();

    for (RefundItemRequest itemRequest : items) {

      // Lock order-item
      OrderItem orderItem = orderItemRepository
          .findBySlugAndOrderForUpdate(itemRequest.getOrderItemSlug(), order)
          .orElseThrow(() -> new ResourceNotFoundException("Order item not found."));

      int requestedQuantity = itemRequest.getQuantity();

      if (requestedQuantity <= 0) {
        throw new PaymentException("Refund quantity must be greater than zero.");
      }

      validateRefundQuantity(orderItem, requestedQuantity);

      BigDecimal itemAmount = orderItem.getPrice().multiply(BigDecimal.valueOf(requestedQuantity));

      RefundItem refundItem = new RefundItem();

      refundItem.setOrderItem(orderItem);
      refundItem.setQuantity(requestedQuantity);
      refundItem.setAmount(itemAmount);
      refundItems.add(refundItem);

      totalRefundAmount = totalRefundAmount.add(itemAmount);

    }

    if (totalRefundAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new PaymentException("Refund amount must be greater than zero.");
    }

    if (totalRefundAmount.compareTo(payment.getAmount()) > 0) {
      throw new PaymentException("Refund amount cannot exceed the payment amount.");
    }

    Refund refund = new Refund();

    refund.setUser(user);
    refund.setPayment(payment);
    refund.setAmount(totalRefundAmount);
    refund.setCurrency(payment.getCurrency());
    refund.setProvider(payment.getPaymentProvider());
    refund.setReference(generateRefundReference());
    refund.setReason(request.getReason());
    refund.setRefundStatus(RefundStatus.REQUESTED);
    refund.setGatewayStatus(RefundGatewayStatus.PENDING);

    for (RefundItem item : refundItems) {
      refund.addItem(item);
    }

    Refund saved = refundRepository.save(refund);

    log.info("Refund {} requested for payment {} with amount {}",
        saved.getReference(),
        payment.getReference(),
        saved.getAmount()
    );

    return convertToDto(saved);
  }


  @Override
  @Transactional
  public RefundPaymentResponse initializeRefund(String refundSlug) {

    Refund refund = getRefundEntity(refundSlug);

    if (refund.getRefundStatus() != RefundStatus.APPROVED) {
      throw new PaymentException("Refund must be approved before it can be initialized.");
    }

    if (refund.getGatewayStatus() == RefundGatewayStatus.PROCESSED) {
      throw new PaymentException("Refund has already been processed.");
    }

    Payment payment = refund.getPayment();
    PaymentGateway gateway = gatewayFactory.get(payment.getPaymentProvider());
    log.info(
        "Refund peter: id={}, slug={}, amount={}, status={}, " +
            "gatewayStatus={}, gatewayRefundId={}, reason={}",
        refund.getId(),
        refund.getSlug(),
        refund.getAmount(),
        refund.getRefundStatus(),
        refund.getGatewayStatus(),
        refund.getGatewayRefundId(),
        refund.getReason()
    );

    log.info(
        "Payment peter: id={}, slug={}, reference={}, transactionId={}, " +
            "amount={}, provider={}, status={}",
        payment.getId(),
        payment.getSlug(),
        payment.getReference(),
        payment.getTransactionId(),
        payment.getAmount(),
        payment.getPaymentProvider(),
        payment.getPaymentStatus()
    );

    BigDecimal amountInKobo = refund.getAmount()
        .multiply(BigDecimal.valueOf(100));

    RefundPaymentRequest request = RefundPaymentRequest.builder()
            .transactionReference(payment.getReference())
            .amount(amountInKobo)
            .reason(refund.getReason())
            .build();

    RefundPaymentResponse response = gateway.refund(request);

    if (!response.isSuccess()) {
      refund.setGatewayStatus(RefundGatewayStatus.FAILED);
      refund.setFailureReason(response.getMessage());
      refundRepository.save(refund);

      throw new PaymentException("Refund initialization failed: " + response.getMessage());
    }

    refund.setGatewayStatus(response.getStatus());
    refund.setGatewayRefundId(response.getRefundReference());
    refund.setGatewayResponse(response.getMessage());

    refundRepository.save(refund);

    return response;
  }


  @Override
  @Transactional
  public RefundPaymentResponse verifyRefund(String refundReference) {

    Refund refund = refundRepository.findByReference(refundReference)
        .orElseThrow(() ->
            new ResourceNotFoundException("Refund not found"));

    PaymentGateway gateway = gatewayFactory.get(refund.getProvider());
    RefundPaymentResponse response = gateway.verifyRefund(refundReference);

    if (response == null) {
      throw new PaymentException("Unable to verify refund status.");
    }

    refund.setGatewayStatus(response.getStatus());

    if (response.getRefundReference() != null) {
      refund.setGatewayRefundId(response.getRefundReference());
    }
    refund.setGatewayResponse(response.getMessage());

    if (response.getStatus() == RefundGatewayStatus.PROCESSED ||
        response.getStatus() == RefundGatewayStatus.SUCCESS) {
      refund.setRefundedAt(Instant.now());
    }

    refundRepository.save(refund);

    return response;
  }


  // WEBHOOK STATUS UPDATES
  @Override
  @Transactional
  public void markPending(PaymentWebhookEvent event) {
    Refund refund = getRefundByPaymentReference(event.transactionReference());
    refund.setGatewayStatus(RefundGatewayStatus.PENDING);

    refundRepository.save(refund);
  }


  @Override
  @Transactional
  public void markProcessing(PaymentWebhookEvent event) {
    Refund refund = getRefundByPaymentReference(event.transactionReference());
    refund.setGatewayStatus(RefundGatewayStatus.PROCESSING);

    refundRepository.save(refund);
  }


  @Override
  @Transactional
  public void markNeedsAttention(PaymentWebhookEvent event) {
    Refund refund = getRefundByPaymentReference(event.transactionReference());
    refund.setGatewayStatus(RefundGatewayStatus.NEEDS_ATTENTION);

    refundRepository.save(refund);
    // Notification can be added here later.
    log.info("Refund {} requires customer attention",
        event.transactionReference());
  }


@Override
@Transactional
public void markSuccessful(PaymentWebhookEvent event) {

  Refund refund = getRefundByPaymentReference(
      event.transactionReference()
  );

  Payment payment = refund.getPayment();
  Order order = payment.getOrder();

  BigDecimal refundedAmount = event.amount();

  if (refundedAmount.compareTo(refund.getAmount()) != 0) {
    throw new PaymentException("Gateway refund amount does not match refund amount.");
  }

  refund.setGatewayStatus(RefundGatewayStatus.PROCESSED);
  refund.setRefundedAt(Instant.now());

  refundRepository.flush(); // see note below

  BigDecimal totalRefunded =
      refundRepository.sumRefundsByPaymentId(payment.getId(),RefundGatewayStatus.PROCESSED);

  BigDecimal paidAmount = payment.getAmount();

  if (totalRefunded.compareTo(paidAmount) > 0) {
    refund.setGatewayStatus(RefundGatewayStatus.NEEDS_ATTENTION); // maybe send message to the admin or something

    refund.setFailureReason("Total refunded amount exceeds payment amount.");

    log.error(
        "OVER-REFUND detected. Payment={}, paid={}, refunded={}",
        payment.getReference(),
        paidAmount,
        totalRefunded
    );

    return;
  }

  if (totalRefunded.compareTo(paidAmount) == 0) {
    payment.setPaymentStatus(PaymentStatus.REFUNDED);
  } else {
    payment.setPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
  }
  updateOrderPaymentStatus(
      order,
      payment.getPaymentStatus()
  );
  paymentRepository.save(payment);
  orderRepository.save(order);

  log.info(
      "Refund {} processed for payment {}. Paid={}, totalRefunded={}, order={}",
      refund.getReference(),
      payment.getReference(),
      paidAmount,
      totalRefunded,
      order.getOrderNumber()
  );
}

  @Override
  @Transactional
  public void markFailed(PaymentWebhookEvent event, String reason) {
    Refund refund = getRefundByPaymentReference(event.transactionReference());
    refund.setGatewayStatus(RefundGatewayStatus.FAILED);
    refund.setFailureReason(reason);

    refundRepository.save(refund);
  }


  // RETRIEVAL
  @Override
  @Transactional(readOnly = true)
  public RefundResponse getRefund(String refundSlug) {
    Refund refund = getRefundEntity(refundSlug);

    return convertToDto(refund);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RefundResponse> getUserRefunds(String userSlug) {
    User user = currentUserService.getCurrentUser();

    return refundRepository.findByUserOrderByCreatedAtDesc(user)
        .stream()
        .map(this::convertToDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<RefundResponse> getOrderRefunds(String orderSlug) {

    Order order = orderRepository.findBySlug(orderSlug)
        .orElseThrow(() ->
            new ResourceNotFoundException("Order not found"));

    return refundRepository.findByPaymentOrderOrderByCreatedAtDesc(order)
        .stream()
        .map(this::convertToDto)
        .toList();
  }


  // ADMIN
  @Override
  @Transactional
  public RefundResponse approveRefund(String refundSlug) {

    Refund refund = getRefundEntity(refundSlug);

    if (refund.getRefundStatus() != RefundStatus.REQUESTED) {
      throw new PaymentException("Only requested refunds can be approved.");
    }

    refund.setRefundStatus(RefundStatus.APPROVED);
    Refund saved = refundRepository.save(refund);

    return convertToDto(saved);
  }

  @Override
  @Transactional
  public RefundResponse rejectRefund(String refundSlug, String reason) {

    Refund refund = getRefundEntity(refundSlug);
    if (refund.getRefundStatus() != RefundStatus.REQUESTED) {
      throw new PaymentException("Only requested refunds can be rejected.");
    }
    refund.setRefundStatus(RefundStatus.REJECTED);
    refund.setFailureReason(reason);
    Refund saved = refundRepository.save(refund);

    return convertToDto(saved);
  }


  // DTO
  @Override
  public RefundResponse convertToDto(Refund refund) {
    return RefundResponse.builder()
        .slug(refund.getSlug())
        .reference(refund.getReference())
        .orderSlug(refund.getPayment().getOrder().getSlug())
        .paymentReference(refund.getPayment().getReference())
        .gatewayRefundId(refund.getGatewayRefundId())
        .amount(refund.getAmount())
        .currency(refund.getCurrency())
        .provider(refund.getProvider())
        .refundStatus(refund.getRefundStatus())
        .gatewayStatus(refund.getGatewayStatus())
        .reason(refund.getReason())
        .failureReason(refund.getFailureReason())
        .refundedAt(refund.getRefundedAt())
        .createdAt(refund.getCreatedAt())
        .items(refund.getItems()
                .stream()
                .map(this::convertRefundItemToDto)
                .toList()
        )
        .build();
  }


  // HELPERS
  private Refund getRefundEntity(String refundSlug) {
    return refundRepository.findBySlug(refundSlug)
        .orElseThrow(() ->
            new ResourceNotFoundException("Refund not found"));
  }

  private Refund getRefundByReference(String reference) {
    return refundRepository.findByReference(reference)
        .orElseThrow(() ->
            new ResourceNotFoundException("Refund not found for reference: " + reference));
  }

  private Refund getRefundByPaymentReference(String paymentReference) {
    return refundRepository.findByPaymentReference(paymentReference)
        .orElseThrow(() ->
            new ResourceNotFoundException(
                "Refund not found for payment reference: " + paymentReference
            )
        );
  }

  private String generateRefundReference() {
    return "REF-" + UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 12)
            .toUpperCase();
  }

  private void updateOrderPaymentStatus(
      Order order,
      PaymentStatus paymentStatus
  ) {
    order.setPaymentStatus(paymentStatus);
  }

  private RefundItemResponse convertRefundItemToDto(RefundItem item) {

    return RefundItemResponse.builder()
        .slug(item.getSlug())
        .orderItemSlug(item.getOrderItem().getSlug())
        .quantity(item.getQuantity())
        .amount(item.getAmount())
        .build();
  }

  private void validateRefundQuantity(
      OrderItem orderItem,
      int requestedQuantity
  ) {

    Integer activeQuantity =
        refundItemRepository.sumActiveRefundQuantityByOrderItemId(
            orderItem.getId()
        );

    int activeRefundQuantity = activeQuantity != null ? activeQuantity : 0;

    int purchasedQuantity = orderItem.getQuantity();

    int availableQuantity = purchasedQuantity - activeRefundQuantity;

    if (requestedQuantity > availableQuantity) {
      throw new PaymentException(
          String.format(
              "Cannot refund %d unit(s). " + "Only %d unit(s) are available for refund.",
              requestedQuantity,
              Math.max(availableQuantity, 0)
          )
      );
    }
  }
}
//NOTE:
//Also, your refundRepository.flush() is different from save() here.
// You need the flush because you’re immediately querying the database
// for sumRefundsByPaymentId() and you want the just-marked refund included in that SUM.
// You don’t need to flush the payment before reading its status because you’re
// reading the Java object, not querying the database.


//      if (requestedQuantity <= 0) {
//        throw new PaymentException("Refund quantity must be greater than zero.");
//      }
//
//      Integer refundedQuantity = refundItemRepository.sumRefundedQuantityByOrderItemId(
//              orderItem.getId(),
//              RefundGatewayStatus.PROCESSED
//          );
//
//      if (refundedQuantity == null) {
//        refundedQuantity = 0;
//      }
//
//      int remainingQuantity = orderItem.getQuantity() - refundedQuantity;
//
//      if (requestedQuantity > remainingQuantity) {
//        throw new PaymentException(
//            "Requested refund quantity exceeds the " + "remaining refundable quantity."
//        );
//      }

//    BigDecimal refundAmount = request.getAmount() != null
//        ? request.getAmount()
//        : payment.getAmount();

//    if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
//      throw new PaymentException("Refund amount must be greater than zero.");
//    }
//
//    if (refundAmount.compareTo(payment.getAmount()) > 0) {
//      throw new PaymentException("Refund amount cannot exceed the payment amount.");
//
//    }

//  @Override
//  @Transactional
//  public void markSuccessful(PaymentWebhookEvent event) {
//    Refund refund = getRefundByPaymentReference(event.transactionReference());
//    refund.setGatewayStatus(RefundGatewayStatus.PROCESSED);
//    refund.setRefundedAt(LocalDateTime.now());
//
//
//    refundRepository.save(refund);
//    log.info(
//        "Refund {} successfully processed for payment {}",
//        refund.getReference(),
//        event.transactionReference()
//    );
//  }
