package com.benkih.estore.payment.service;

import com.benkih.estore.audit.service.ApiLogService;
import com.benkih.estore.common.enums.*;
import com.benkih.estore.common.exceptions.*;
import com.benkih.estore.notification.INotificationService;
import com.benkih.estore.order.service.OrderService;
import com.benkih.estore.payment.dto.request.CheckoutRequest;
import com.benkih.estore.payment.dto.request.InitializePaymentRequest;
import com.benkih.estore.payment.dto.response.InitializePaymentResponse;
import com.benkih.estore.payment.dto.response.PaymentResponse;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.repository.OrderRepository;
import com.benkih.estore.payment.dto.response.RefundPaymentResponse;
import com.benkih.estore.payment.dto.response.VerifyPaymentResponse;
import com.benkih.estore.payment.dto.webhook.PaymentWebhookEvent;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.payment.entity.PaymentEvent;
import com.benkih.estore.payment.provider.PaymentGateway;
import com.benkih.estore.payment.provider.PaymentGatewayFactory;
import com.benkih.estore.payment.provider.PaymentWebhookHandler;
import com.benkih.estore.payment.provider.PaymentWebhookHandlerFactory;
import com.benkih.estore.payment.repository.PaymentEventRepository;
import com.benkih.estore.payment.repository.PaymentRepository;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

  @Value("${app.payment.callback-url}")
  private String callbackUrl;
  @Value("${app.payment.timeout.minutes:5}")
  private int paymentTimeoutMinutes;
  @Value("${api.prefix}")
  private String apiPrefix;

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final PaymentGatewayFactory gatewayFactory;
  private final CurrentUserService currentUserService;
  private final OrderService orderService;
  private final PaymentEventRepository paymentEventRepository;
  private final PaymentWebhookHandlerFactory paymentWebhookHandlerFactory;
  private final ApiLogService apiLogService;
  private final INotificationService notificationService;


  @Override
  public InitializePaymentResponse checkout(CheckoutRequest request) {
    Order order = orderRepository.findBySlug(request.getOrderSlug())
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
//    log.info("here is the order data={}",order);

    if (!order.getUser().getSlug().equals(currentUserService.getCurrentUserSlug())) {
      throw new ForbiddenException("You are not allowed to pay for this order.");
    }

    //  Check if order can be paid
    orderService.validateOrderCanBePaid(order);

    Optional<InitializePaymentResponse> existingPaymentResponse = handleExistingPayment(order);

    if (existingPaymentResponse.isPresent()) {
      return existingPaymentResponse.get();
    }

    // Create new payment
    Payment payment = createPayment(order, request);
    log.info("Payment created with reference: {}", payment.getReference());

    return  initializePaymentWithGateway(payment, order);
  }


  @Override
  @Transactional
  public PaymentResponse verify(String reference) {
    User user = currentUserService.getCurrentUser();

    Payment payment = paymentRepository.findByReference(reference)
        .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

    if (!payment.getUser().getId().equals(user.getId())) {
      throw new ForbiddenException("You are not allowed to verify this payment.");
    }

    PaymentGateway gateway = gatewayFactory.get(payment.getPaymentProvider());
    VerifyPaymentResponse response = gateway.verify(reference);
    synchronizePayment(payment, response);

    return convertToDto(payment);
  }


  @Transactional
  public void handleWebhook(
      PaymentProvider provider,
      String signature,
      String payload,
      String endpoint
  ) {

    try {
      PaymentWebhookHandler handler = paymentWebhookHandlerFactory.get(provider);

      handler.verifySignature(signature, payload);

      PaymentWebhookEvent event = handler.parseWebhook(payload);

      switch (event.eventType()) {

        case "charge.success":
        case "charge.failed":
          handleChargeEvent(handler, event, signature, payload);
          break;

        case "transfer.success":
        case "transfer.failed":
//          handleTransferEvent(event, payload);
          break;

        case "customeridentification.success":
//          handleCustomerIdentificationEvent(event, payload);
          break;

        case "dedicatedaccount.assign.success":
//          handleDedicatedAccountAssignedEvent(event, payload);
          break;

        default:
          log.info("Ignoring event {}", event.eventType());

          apiLogService.saveInboundLog(
              "POST",
              endpoint,
              payload,
              200,
              "Ignored event: " + event.eventType(),
              null
          );
          return;
      }

      apiLogService.saveInboundLog(
          "POST",
          endpoint,
          payload,
          200,
          "Webhook processed successfully",
          null
      );

    } catch (Exception e) {

      apiLogService.saveInboundLog(
          "POST",
          endpoint,
          payload,
          500,
          null,
          e
      );

      throw e;
    }
  }


  @Override
  public PaymentResponse convertToDto(Payment payment) {
    return new PaymentResponse(
        payment.getSlug(),
        payment.getReference(),
        payment.getOrder().getSlug(),
        payment.getAmount(),
        payment.getCurrency(),
        payment.getPaymentMethod(),
        payment.getPaymentProvider(),
        payment.getPaymentStatus(),
        payment.getTransactionId(),
        payment.getGatewayResponse(),
        payment.getPaidAt()
    );
  }

  private Optional<InitializePaymentResponse> handleExistingPayment(Order order) {
    Optional<Payment> latestPayment = paymentRepository.findTopByOrderOrderByCreatedAtDesc(order);
    log.info("Latest payment={}", latestPayment);

    if (latestPayment.isEmpty()) {
      return Optional.empty();
    }

    Payment payment = latestPayment.get();
    switch (payment.getPaymentStatus()) {
      case SUCCESS -> {
        throw new PaymentException("This order has already been paid.");
      }
      case PENDING -> {
        if (payment.getAuthorizationUrl() != null && payment.getAccessCode() != null) {

          log.info("Returning existing payment for order {}", order.getSlug());

          return Optional.of(
              InitializePaymentResponse.builder()
                  .authorizationUrl(payment.getAuthorizationUrl())
                  .accessCode(payment.getAccessCode())
                  .reference(payment.getReference())
                  .build()
          );
        }

        if (payment.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(paymentTimeoutMinutes))) {
          log.warn("Pending payment {} expired", payment.getReference());
          payment.setPaymentStatus(PaymentStatus.EXPIRED);
          payment.setFailureReason("Payment attempt expired");
          paymentRepository.save(payment);

          return Optional.empty();
        }

        throw new PaymentException("A payment for this order is already in progress.");
      }

      case FAILED, EXPIRED -> {
//        log.info("Previous payment {} is {}, creating a new payment.", payment.getReference(), payment.getPaymentStatus());
        return Optional.empty();
      }

      default -> {
//        log.warn("Unhandled payment status {}", payment.getPaymentStatus());
        throw new PaymentException("Unable to process payment at this time.");
      }
    }
  }


  private InitializePaymentResponse initializePaymentWithGateway(Payment payment, Order order) {
    try {
      PaymentGateway gateway = gatewayFactory.get(payment.getPaymentProvider());
      InitializePaymentRequest request =
          InitializePaymentRequest.builder()
              .email(order.getUser().getEmail())
              .amount(order.getTotalAmount())
              .currency(order.getCurrency())
              .reference(payment.getReference())
              .callbackUrl(callbackUrl)
              .build();

      InitializePaymentResponse response = gateway.initialize(request);

      if (!response.isSuccess()) {
        markPaymentFailed(payment, response.getMessage());
        throw new PaymentException("Payment initialization failed: " + response.getMessage());
      }

      payment.setAuthorizationUrl(response.getAuthorizationUrl());
      payment.setAccessCode(response.getAccessCode());
      payment.setGatewayResponse(response.getMessage());
      payment.setPaymentStatus(PaymentStatus.PENDING);

      paymentRepository.save(payment);
//      log.info("Payment {} initialized successfully", payment.getReference());

      return InitializePaymentResponse.builder()
          .authorizationUrl(payment.getAuthorizationUrl())
          .accessCode(payment.getAccessCode())
          .reference(payment.getReference())
          .build();

    } catch (PaymentGatewayException ex) {
      markPaymentFailed(payment, ex.getMessage());
      throw new PaymentGatewayException("Payment gateway is currently unavailable. Please try again later.", ex);
    } catch (Exception ex) {
      markPaymentFailed(payment, ex.getMessage());
      throw new PaymentException("Payment initialization failed.", ex);
    }
  }


  private void markPaymentFailed(Payment payment, String reason) {
    payment.setPaymentStatus(PaymentStatus.FAILED);
    payment.setFailureReason(reason);
    paymentRepository.save(payment);
  }


  private void synchronizePayment(Payment payment, VerifyPaymentResponse response) {
    if (payment.getPaymentStatus() != response.getStatus()) {
      payment.setPaymentStatus(response.getStatus());
      payment.setTransactionId(response.getTransactionId());
      payment.setGatewayResponse(response.getGatewayResponse());
      payment.setPaidAt(response.getPaidAt());
    }
  }


  private Payment createPayment(Order order, CheckoutRequest request) {
    Payment payment = new Payment();
    payment.setReference(generateReference()); // Use proper reference generation
    payment.setAmount(order.getTotalAmount());
    payment.setCurrency(order.getCurrency() != null ? order.getCurrency() : Currency.NGN);
    payment.setOrder(order);
    payment.setUser(order.getUser());
    payment.setPaymentMethod(request.getPaymentMethod());
    payment.setPaymentProvider(request.getPaymentProvider());
    payment.setPaymentStatus(PaymentStatus.PENDING);
    payment.setCreatedBy(order.getUser().getSlug());

    return paymentRepository.save(payment);
  }


  private void handleChargeEvent(
      PaymentWebhookHandler handler,
      PaymentWebhookEvent event,
      String signature,
      String payload
  ) {

    if (shouldIgnore(event)) {
      return;
    }

    Payment payment = getPayment(event.reference());

    if (isDuplicateWebhook(payment, event, payload)) {
      return;
    }

    saveWebhookEvent(
        payment,
        event.eventType(),
        payload,
        signature
    );

    VerifyPaymentResponse response = handler.verify(event.reference());

    synchronizePayment(payment, response);

    processOrder(payment);

    postPaymentProcessing(payment);

    saveFinalPaymentEvent(payment, event, response);
  }


  private String generateReference() {
    return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
  }

  //  Save webhook event
  private void saveWebhookEvent(Payment payment, String eventType, String payload, String signature) {
    PaymentEvent event = new PaymentEvent();
    event.setPayment(payment);
    event.setEventType(PaymentEventType.WEBHOOK_RECEIVED);
    event.setPayload(payload);
    event.setCreatedBy("WEBHOOK");
    paymentEventRepository.save(event);
    log.info("Webhook event saved for payment: {}", payment.getReference());
  }

  //  Save payment event
  private void savePaymentEvent(Payment payment, PaymentEventType eventType, String details) {
    PaymentEvent event = new PaymentEvent();
    event.setPayment(payment);
    event.setEventType(eventType);
    event.setPayload(details);
    event.setCreatedBy("SYSTEM");
    paymentEventRepository.save(event);
  }

  // Save payment event with payload
  private void savePaymentEvent(Payment payment, PaymentEventType eventType, String details, String payload) {
    PaymentEvent event = new PaymentEvent();
    event.setPayment(payment);
    event.setEventType(eventType);
    event.setPayload(payload != null ? payload : details);
    event.setCreatedBy("SYSTEM");
    paymentEventRepository.save(event);
  }


  private boolean shouldIgnore(PaymentWebhookEvent event) {
    String eventType = event.eventType();
    if (!"charge.success".equals(eventType) && !"charge.failed".equals(eventType)) {
      log.info("Ignoring webhook event {}", eventType);
      return true;
    }
    return false;
  }


  private Payment getPayment(String reference) {
    return paymentRepository.findByReference(reference)
        .orElseThrow(() -> new ResourceNotFoundException("Payment not found for reference: " + reference));
  }


  private boolean isDuplicateWebhook(Payment payment, PaymentWebhookEvent event, String payload) {
    String reference = payment.getReference();
    String transactionId = String.valueOf(payment.getTransactionId());

    if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
      log.info("Payment {} already successful", reference);
      saveWebhookEvent(payment, event.eventType(), payload, null);
      return true;
    }

    if (payment.getTransactionId() != null && payment.getTransactionId().equals(transactionId)) {
      log.info("Transaction {} already processed", transactionId);
      saveWebhookEvent(payment, event.eventType(), payload, null);
      return true;
    }
    return false;
  }


private void processOrder(Payment payment) {
  if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
    return;
  }
  orderService.processPaidOrder(payment.getOrder());
}

  private void postPaymentProcessing(Payment payment) {
    if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
      return;
    }
//    eventPublisher.publishEvent(new PaymentReceiptEvent(payment));
//    eventPublisher.publishEvent(new OrderConfirmationEvent(payment.getOrder()));
//    notificationService.sendPaymentReceipt(payment);
//    sleepForMailtrap();
    notificationService.sendOrderConfirmation(payment.getOrder());
  }
//  private void sleepForMailtrap() {
//    try {
//      Thread.sleep(2000);
//    } catch (InterruptedException e) {
//      Thread.currentThread().interrupt();
//    }
//  }

  private void saveFinalPaymentEvent(Payment payment, PaymentWebhookEvent event, VerifyPaymentResponse response) {
    PaymentEventType type = payment.getPaymentStatus() == PaymentStatus.SUCCESS
            ? PaymentEventType.SUCCESS
            : PaymentEventType.FAILED;

    String message = payment.getPaymentStatus() == PaymentStatus.SUCCESS
            ? "Payment successful via webhook: " + event.eventType()
            : "Payment failed via webhook: " + event.eventType() + " - " + response.getGatewayResponse();

    savePaymentEvent(payment, type, message);
  }
}