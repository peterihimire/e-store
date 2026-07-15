package com.benkih.estore.payment.service;

import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.common.exceptions.ForbiddenException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
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
import com.benkih.estore.payment.provider.PaymentGateway;
import com.benkih.estore.payment.provider.PaymentGatewayFactory;
import com.benkih.estore.payment.repository.PaymentRepository;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.vendor.PaystackClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final PaymentGatewayFactory gatewayFactory;
  private final CurrentUserService currentUserService;
  private final OrderService orderService;
  private final PaystackClient paystackClient;

  @Override
  public InitializePaymentResponse checkout(CheckoutRequest request) {

    Order order = orderRepository.findBySlug(request.getOrderSlug())
        .orElseThrow();
    log.info("here is the order data={}",order);

    if (!order.getUser().getSlug().equals(currentUserService.getCurrentUserSlug())) {
      throw new ForbiddenException("You are not allowed to pay for this order.");
    }

    Payment payment = new Payment();

    payment.setReference(UUID.randomUUID().toString());
    payment.setAmount(order.getTotalAmount());
    payment.setCurrency(order.getCurrency());
    payment.setOrder(order);
    payment.setUser(order.getUser());

    payment.setPaymentMethod(request.getPaymentMethod());
    payment.setPaymentProvider(request.getPaymentProvider());

    paymentRepository.save(payment);

    PaymentGateway gateway = gatewayFactory.get(request.getPaymentProvider());

    InitializePaymentRequest gatewayRequest =
        InitializePaymentRequest.builder()
            .email(order.getUser().getEmail())
            .amount(order.getTotalAmount())
            .currency(order.getCurrency())
            .reference(payment.getReference())
            .callbackUrl("http://localhost:8080/api/v1/payments/callback")
            .build();

    return gateway.initialize(gatewayRequest);
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
      log.info("execution got here=");
    // 1. Verify the webhook really came from Paystack
    if (!paystackClient.verifyWebhookSignature(signature, payload)) {
      throw new IllegalArgumentException("Invalid webhook signature");
    }

    // 2. Parse the JSON payload
    PaystackWebhookEvent event = paystackClient.parseWebhook(payload);
    log.info("Lets see the event={}", event);

    // 3. Ignore events you don't care about
    if (!"charge.success".equals(event.getEvent())) {
      return;
    }

    // 4. Find the payment using the reference sent by Paystack
    Payment payment = paymentRepository.findByReference(event.getData().getReference())
        .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    log.info("Lets see the payment={}", payment);

    // 5. Verify directly with Paystack (recommended)
    VerifyPaymentResponse response = paystackClient.verify(payment.getReference());
      log.info("Lets see the paystack client info response={}", response);

    log.info("Synchronizing payment...");
    // 6. Update the payment
    synchronizePayment(payment, response);
    log.info("Payment synchronized. Status={}", payment.getPaymentStatus());

    // 7. Mark the order as paid if payment succeeded
    if (response.getStatus() == PaymentStatus.SUCCESS) {
      log.info("Marking order {} as paid", payment.getOrder().getSlug());
      orderService.markAsPaid(payment.getOrder());
      log.info("Order marked as paid");
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


  private void synchronizePayment(Payment payment, VerifyPaymentResponse response) {

    if (payment.getPaymentStatus() != response.getStatus()) {
      payment.setPaymentStatus(response.getStatus());
      payment.setTransactionId(response.getTransactionId());
      payment.setGatewayResponse(response.getGatewayResponse());
      payment.setPaidAt(response.getPaidAt());
    }
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