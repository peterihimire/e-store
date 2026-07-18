package com.benkih.estore.payment.service;

import com.benkih.estore.common.enums.Currency;
import com.benkih.estore.common.enums.OrderStatus;
import com.benkih.estore.common.enums.PaymentEventType;
import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.common.exceptions.*;
import com.benkih.estore.order.service.OrderService;
import com.benkih.estore.payment.dto.request.CheckoutRequest;
import com.benkih.estore.payment.dto.request.InitializePaymentRequest;
import com.benkih.estore.payment.dto.response.InitializePaymentResponse;
import com.benkih.estore.payment.dto.response.PaymentResponse;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.repository.OrderRepository;
import com.benkih.estore.payment.dto.response.VerifyPaymentResponse;
import com.benkih.estore.payment.dto.webhook.PaystackWebhookEvent;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.payment.entity.PaymentEvent;
import com.benkih.estore.payment.provider.PaymentGateway;
import com.benkih.estore.payment.provider.PaymentGatewayFactory;
import com.benkih.estore.payment.repository.PaymentEventRepository;
import com.benkih.estore.payment.repository.PaymentRepository;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.vendor.PaystackClient;
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

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final PaymentGatewayFactory gatewayFactory;
  private final CurrentUserService currentUserService;
  private final OrderService orderService;
  private final PaystackClient paystackClient;
  private final PaymentEventRepository paymentEventRepository;


  @Override
  public InitializePaymentResponse checkout(CheckoutRequest request) {
    Order order = orderRepository.findBySlug(request.getOrderSlug())
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    log.info("here is the order data={}",order);

    if (!order.getUser().getSlug().equals(currentUserService.getCurrentUserSlug())) {
      throw new ForbiddenException("You are not allowed to pay for this order.");
    }

    //  Check if order can be paid
    validateOrderCanBePaid(order);

    Optional<InitializePaymentResponse> existingPaymentResponse = handleExistingPayment(order);

//    if (existingPaymentResponse.isPresent()) {
//      return existingPaymentResponse.get();
//    }

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
  public void handlePaystackWebhook(String signature, String payload) {
    log.info("Received Paystack webhook");

    verifyWebhook(signature, payload);
    PaystackWebhookEvent event = paystackClient.parseWebhook(payload);

    if (shouldIgnore(event)) {
      return;
    }

    String reference = event.getData().getReference();

    Payment payment = getPayment(reference);
    if (isDuplicateWebhook(payment, event, payload)) {
      return;
    }

    saveWebhookEvent(payment, event.getEvent(), payload, signature);
    VerifyPaymentResponse response = paystackClient.verify(reference);
    synchronizePayment(payment, response);

    processOrder(payment);
    saveFinalPaymentEvent(payment, event, response);
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
      return null;
    }

    Payment payment = latestPayment.get();
    switch (payment.getPaymentStatus()) {
      case SUCCESS -> {
        throw new PaymentException("This order has already been paid.");
      }
      case PENDING -> {
        // Return existing checkout if Paystack initialization succeeded
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

        // Payment initialization never completed
        if (payment.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(paymentTimeoutMinutes))) {
          log.warn("Pending payment {} expired", payment.getReference());
          payment.setPaymentStatus(PaymentStatus.EXPIRED);
          payment.setFailureReason("Payment attempt expired");
          paymentRepository.save(payment);
          return null;
        }

        throw new PaymentException("A payment for this order is already in progress.");
      }

      case FAILED, EXPIRED -> {
        log.info("Previous payment {} is {}, creating a new payment.", payment.getReference(), payment.getPaymentStatus());
        return null;
      }

      default -> {
        log.warn("Unhandled payment status {}", payment.getPaymentStatus());
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

      log.info("Payment {} initialized successfully", payment.getReference());

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

  private String generateReference() {
    return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
  }

  private void validateOrderCanBePaid(Order order) {
    if (order.getOrderStatus() == OrderStatus.PAID) {
      log.info("Order validate...={}", order.getSlug());
      throw new DuplicatePaymentException(String.format("Order %s has already been paid for.", order.getSlug())
      );
    }

    if (order.getOrderStatus() == OrderStatus.PROCESSING ||
        order.getOrderStatus() == OrderStatus.SHIPPED ||
        order.getOrderStatus() == OrderStatus.DELIVERED) {
      throw new PaymentException(
          String.format("Order %s is already being processed and cannot be paid for.", order.getSlug())
      );
    }

    if (order.getOrderStatus() == OrderStatus.CANCELLED) {
      throw new PaymentException(
          String.format("Order %s has been cancelled.", order.getSlug())
      );
    }

    if (order.getOrderStatus() == OrderStatus.EXPIRED) {
      throw new PaymentException(
          String.format("Order %s has expired.", order.getSlug())
      );
    }
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

  private void verifyWebhook(String signature, String payload) {
    if (!paystackClient.verifyWebhookSignature(signature, payload)) {
      log.error("Invalid Paystack webhook signature");
      throw new IllegalArgumentException("Invalid webhook signature");
    }
  }

  private boolean shouldIgnore(PaystackWebhookEvent event) {
    String eventType = event.getEvent();
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

  private boolean isDuplicateWebhook(Payment payment, PaystackWebhookEvent event, String payload) {
    String reference = event.getData().getReference();
    String transactionId = String.valueOf(event.getData().getId());

    if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
      log.info("Payment {} already successful", reference);
      saveWebhookEvent(payment, event.getEvent(), payload, null);
      return true;
    }

    if (payment.getTransactionId() != null && payment.getTransactionId().equals(transactionId)) {
      log.info("Transaction {} already processed", transactionId);
      saveWebhookEvent(payment, event.getEvent(), payload, null);
      return true;
    }
    return false;
  }

  private void processOrder(Payment payment) {
    if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
      log.info("Marking order {} as paid", payment.getOrder().getSlug());
      orderService.markAsPaid(payment.getOrder());
    }
  }

  private void saveFinalPaymentEvent(Payment payment, PaystackWebhookEvent event, VerifyPaymentResponse response) {
    PaymentEventType type = payment.getPaymentStatus() == PaymentStatus.SUCCESS
            ? PaymentEventType.SUCCESS
            : PaymentEventType.FAILED;

    String message = payment.getPaymentStatus() == PaymentStatus.SUCCESS
            ? "Payment successful via webhook: " + event.getEvent()
            : "Payment failed via webhook: " + event.getEvent() + " - " + response.getGatewayResponse();

    savePaymentEvent(payment, type, message);
  }
}




//  private Payment synchronizePayment(Payment payment) {
//    PaymentGateway gateway = gatewayFactory.get(payment.getPaymentProvider());
//    VerifyPaymentResponse response = gateway.verify(payment.getReference());
//
//    if (payment.getPaymentStatus() != response.getStatus()) {
//      payment.setPaymentStatus(response.getStatus());
//      payment.setTransactionId(response.getTransactionId());
//      payment.setGatewayResponse(response.getGatewayResponse());
//      payment.setPaidAt(response.getPaidAt());
//      //      paymentRepository.save(payment);
//    }
//
//    return payment;
//  }

//  @Transactional
//  public void handlePaystackWebhook(String signature, String payload) {
//
//    if (!paystackClient.verifyWebhookSignature(signature, payload)) {
//      throw new IllegalArgumentException("Invalid webhook signature");
//    }
//
////    PaystackWebhookEvent event = paystackClient.parseWebhook(payload);
//
//    if (!"charge.success".equals(event.getEvent())) {
//      return;
//    }
//
//    Payment payment = paymentRepository
//        .findByReference(event.getData().getReference())
//        .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
//
//    VerifyPaymentResponse response = paystackClient.verify(payment.getReference());
//
//    synchronizePayment(payment, response);
//
//    orderService.markAsPaid(payment.getOrder());
//  }

//    if (existing.isPresent()) {
//      Payment payment = existing.get();
//
//      // If payment already has authorizationUrl, return it (idempotent)
//      if (payment.getAuthorizationUrl() != null && payment.getAccessCode() != null) {
//        log.info("Returning existing payment for order: {}", order.getSlug());
//        return InitializePaymentResponse.builder() // had to add @Builder annotation to use it
//            .authorizationUrl(payment.getAuthorizationUrl())
//            .accessCode(payment.getAccessCode())
//            .reference(payment.getReference())
//            .build();
//
//                return new InitializePaymentResponse(
//                    existing.get().getAuthorizationUrl(),
//                    existing.get().getAccessCode(),
//                    existing.get().getReference()
//                );
//      }
//
//      // If payment is stale (> 5 minutes), mark as expired and create new
//      if (payment.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(paymentTimeoutMinutes))) {
//        log.warn("Stale payment found for order: {}, marking as expired", order.getSlug());
//        payment.setPaymentStatus(PaymentStatus.EXPIRED);
//        payment.setFailureReason("Payment attempt expired");
//        paymentRepository.save(payment);
//
//        // Continue to create new payment
//      } else {
//        // Still within 5 minutes, but no authorizationUrl - something went wrong
//        log.error("Payment exists but missing gateway details: {}", payment.getReference());
//        payment.setPaymentStatus(PaymentStatus.FAILED);
//        payment.setFailureReason("Payment initiation failed - missing gateway response");
//        paymentRepository.save(payment);
//
//        throw new PaymentException("Payment initiation failed. Please try again.");
//      }
//    }


//    try {
//      PaymentGateway gateway = gatewayFactory.get(request.getPaymentProvider());
//
//      InitializePaymentRequest gatewayRequest = InitializePaymentRequest.builder()
//          .email(order.getUser().getEmail())
//          .amount(order.getTotalAmount())
//          .currency(order.getCurrency())
//          .reference(payment.getReference())
//          .callbackUrl(callbackUrl)
//          .build();
//
//      InitializePaymentResponse gatewayResponse = gateway.initialize(gatewayRequest);
//      log.info("Gateway response received for reference: {}", payment.getReference());
//
//
//      // Check if gateway initialization was successful
//      if (!gatewayResponse.isSuccess()) {
//        log.error("Gateway initialization failed: {}", gatewayResponse.getMessage());
//
//        payment.setPaymentStatus(PaymentStatus.FAILED);
//        payment.setFailureReason(gatewayResponse.getMessage());
//        payment.setGatewayResponse(gatewayResponse.getMessage());
//        paymentRepository.save(payment);
//
//        throw new PaymentException("Payment initialization failed: " + gatewayResponse.getMessage());
//      }
//
//      //  Update payment with complete gateway response
//      payment.setAuthorizationUrl(gatewayResponse.getAuthorizationUrl());
//      payment.setAccessCode(gatewayResponse.getAccessCode());
//      payment.setGatewayResponse(gatewayResponse.getMessage());
//      payment.setPaymentStatus(PaymentStatus.PENDING);
//
//      paymentRepository.save(payment);
//      log.info("Payment updated with gateway details: {}", payment.getReference());
//
//      return InitializePaymentResponse.builder()
//          .authorizationUrl(payment.getAuthorizationUrl())
//          .accessCode(payment.getAccessCode())
//          .reference(payment.getReference())
//          .build();
//
//    } catch (PaymentGatewayException e) {
//      log.error("Payment gateway error: {}", e.getMessage(), e);
//
//      payment.setPaymentStatus(PaymentStatus.FAILED);
//      payment.setFailureReason(e.getMessage());
//      paymentRepository.save(payment);
//
//      throw new PaymentGatewayException("Payment gateway is currently unavailable. Please try again later.", e);
//
//    } catch (Exception e) {
//      log.error("Payment initialization error: {}", e.getMessage(), e);
//
//      payment.setPaymentStatus(PaymentStatus.FAILED);
//      payment.setFailureReason(e.getMessage());
//      paymentRepository.save(payment);
//
//      throw new PaymentException("Payment initialization failed: " + e.getMessage(), e);
//    }

//  @Transactional
//  public void handlePaystackWebhook(String signature, String payload) {
//      log.info("execution got here=");
//    // 1. Verify the webhook really came from Paystack
//    if (!paystackClient.verifyWebhookSignature(signature, payload)) {
//      throw new IllegalArgumentException("Invalid webhook signature");
//    }
//    // 2. Parse the JSON payload
//    PaystackWebhookEvent event = paystackClient.parseWebhook(payload);
//    log.info("Lets see the event={}", event);
//    // 3. Ignore events you don't care about
//    // 3. Ignore events we don't care about
//    if (!"charge.success".equals(event.getEvent()) && !"charge.failed".equals(event.getEvent())) {
//      log.info("Ignoring webhook event: {}", event.getEvent());
//      return;
//    }
//    // 4. Find the payment
//    String reference = event.getData().getReference();
//    Payment payment = paymentRepository.findByReference(reference)
//        .orElseThrow(() -> new ResourceNotFoundException("Payment not found for reference: " + reference));
//    log.info("Lets see the payment={}", payment);
//    // 5. Verify directly with Paystack (recommended)
//    VerifyPaymentResponse response = paystackClient.verify(payment.getReference());
//      log.info("Lets see the paystack client info response={}", response);
//
//    log.info("Synchronizing payment...");
//    // 6. Update the payment
//    synchronizePayment(payment, response);
//    log.info("Payment synchronized. Status={}", payment.getPaymentStatus());
//
//    // 7. Mark the order as paid if payment succeeded
//    if (response.getStatus() == PaymentStatus.SUCCESS) {
//      log.info("Marking order {} as paid", payment.getOrder().getSlug());
//      orderService.markAsPaid(payment.getOrder());
//      log.info("Order marked as paid");
//    }
//  }


//  @Transactional
//  public void handlePaystackWebhook(String signature, String payload) {
//    log.info("Received Paystack webhook");
//
//    // 1. Verify the webhook signature
//    if (!paystackClient.verifyWebhookSignature(signature, payload)) {
//      log.error("Invalid webhook signature");
//      throw new IllegalArgumentException("Invalid webhook signature");
//    }
//
//    // 2. Parse the JSON payload
//    PaystackWebhookEvent event = paystackClient.parseWebhook(payload);
//    String reference = event.getData().getReference();
//    String eventType = event.getEvent();
//
//    log.info("Webhook event: {}, reference: {}", eventType, reference);
//
//    // 3. Ignore events we don't care about
//    if (!"charge.success".equals(eventType) && !"charge.failed".equals(eventType)) {
//      log.info("Ignoring webhook event: {}", eventType);
//      return;
//    }
//
//    // 4. Find the payment
//    Payment payment = paymentRepository.findByReference(reference)
//        .orElseThrow(() -> new ResourceNotFoundException("Payment not found for reference: " + reference));
//
//    // ✅ 5. IDEMPOTENCY CHECK #1: Check if payment is already successful
//    if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
//      log.info("Payment already successful. Webhook idempotent: {}", reference);
//      // ✅ Save webhook event for audit anyway
//      saveWebhookEvent(payment, eventType, payload, signature);
//      return;
//    }
//
//    // ✅ 6. IDEMPOTENCY CHECK #2: Check if this webhook was already processed
//    boolean webhookAlreadyProcessed = payment.getEvents().stream()
//        .anyMatch(e -> e.getEventType() == PaymentEventType.WEBHOOK_RECEIVED &&
//            e.getPayload() != null &&
//            e.getPayload().contains(reference));
//
//    if (webhookAlreadyProcessed) {
//      log.info("Webhook already processed for reference: {}", reference);
//      return;
//    }
//
//    // ✅ 7. IDEMPOTENCY CHECK #3: Check if this transaction was already processed
//    String transactionId = String.valueOf(event.getData().getId());
//    if (payment.getTransactionId() != null && payment.getTransactionId().equals(transactionId)) {
//      log.info("Transaction already processed. Webhook idempotent: {}", reference);
//      saveWebhookEvent(payment, eventType, payload, signature);
//      return;
//    }
//
//    log.info("Processing webhook for payment: {}, event: {}, transactionId: {}",
//        reference, eventType, transactionId);
//
//    // ✅ 8. Save webhook event BEFORE processing (for idempotency)
//    saveWebhookEvent(payment, eventType, payload, signature);
//
//    // 9. Verify directly with Paystack (recommended)
//    VerifyPaymentResponse response = paystackClient.verify(reference);
//    log.info("Paystack verification response: status={}", response.getStatus());
//
//    // 10. Synchronize payment
//    synchronizePayment(payment, response);
//
//    // 11. Update order if payment succeeded
//    if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
//      log.info("Marking order {} as paid", payment.getOrder().getSlug());
//      orderService.markAsPaid(payment.getOrder());
//      log.info("Order marked as paid");
//
//      // ✅ Save success event
//      savePaymentEvent(payment, PaymentEventType.SUCCESS, "Payment successful via webhook: " + eventType);
//    } else {
//      // ✅ Save failure event
//      savePaymentEvent(payment, PaymentEventType.FAILED, "Payment failed via webhook: " + eventType + " - " + response);
//    }
//  }