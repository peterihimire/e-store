package com.benkih.estore.refund.service;


import com.benkih.estore.audit.service.ApiLogService;
import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.common.enums.RefundGatewayStatus;
import com.benkih.estore.common.enums.RefundStatus;
import com.benkih.estore.common.exceptions.ForbiddenException;
import com.benkih.estore.common.exceptions.PaymentException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.notification.INotificationService;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.repository.OrderRepository;
import com.benkih.estore.payment.dto.request.RefundPaymentRequest;
import com.benkih.estore.payment.dto.response.RefundPaymentResponse;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.payment.provider.PaymentGateway;
import com.benkih.estore.payment.provider.PaymentGatewayFactory;
import com.benkih.estore.payment.repository.PaymentRepository;
import com.benkih.estore.refund.dto.request.CreateRefundRequest;
import com.benkih.estore.refund.dto.response.RefundResponse;
import com.benkih.estore.refund.entity.Refund;
import com.benkih.estore.refund.repository.RefundRepository;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    BigDecimal refundAmount = request.getAmount() != null
        ? request.getAmount()
        : payment.getAmount();

    if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new PaymentException("Refund amount must be greater than zero.");
    }

    if (refundAmount.compareTo(payment.getAmount()) > 0) {
      throw new PaymentException("Refund amount cannot exceed the payment amount.");

    }

    Refund refund = new Refund();

    refund.setUser(user);
    refund.setPayment(payment);
    refund.setAmount(refundAmount);
    refund.setCurrency(payment.getCurrency());
    refund.setProvider(payment.getPaymentProvider());
    refund.setReference(generateRefundReference());
    refund.setReason(request.getReason());
    refund.setRefundStatus(RefundStatus.REQUESTED);
    refund.setGatewayStatus(RefundGatewayStatus.PENDING);

    Refund saved = refundRepository.save(refund);

    log.info("Refund {} requested for payment {}",
        saved.getReference(),
        payment.getReference()
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
      refund.setRefundedAt(LocalDateTime.now());
    }

    refundRepository.save(refund);

    return response;
  }


  // WEBHOOK STATUS UPDATES
  @Override
  @Transactional
  public void markPending(String transactionReference) {
    Refund refund = getRefundByPaymentReference(transactionReference);
    refund.setGatewayStatus(RefundGatewayStatus.PENDING);

    refundRepository.save(refund);
  }

  @Override
  @Transactional
  public void markProcessing(String transactionReference) {
    Refund refund = getRefundByPaymentReference(transactionReference);
    refund.setGatewayStatus(RefundGatewayStatus.PROCESSING);

    refundRepository.save(refund);
  }

  @Override
  @Transactional
  public void markNeedsAttention(String transactionReference) {
    Refund refund = getRefundByPaymentReference(transactionReference);
    refund.setGatewayStatus(RefundGatewayStatus.NEEDS_ATTENTION);

    refundRepository.save(refund);
    // Notification can be added here later.
    log.info("Refund {} requires customer attention", transactionReference);
  }

  @Override
  @Transactional
  public void markFailed(String transactionReference, String reason) {
    Refund refund = getRefundByPaymentReference(transactionReference);
    refund.setGatewayStatus(RefundGatewayStatus.FAILED);
    refund.setFailureReason(reason);

    refundRepository.save(refund);
  }

  @Override
  @Transactional
  public void markSuccessful(String transactionReference) {
    Refund refund = getRefundByPaymentReference(transactionReference);
    refund.setGatewayStatus(RefundGatewayStatus.PROCESSED);
    refund.setRefundedAt(LocalDateTime.now());

    refundRepository.save(refund);
    log.info(
        "Refund {} successfully processed for payment {}",
        refund.getReference(),
        transactionReference
    );
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
}
