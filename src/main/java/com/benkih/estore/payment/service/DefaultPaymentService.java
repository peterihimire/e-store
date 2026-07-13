package com.benkih.estore.payment.service;

import com.benkih.estore.payment.dto.request.CheckoutRequest;
import com.benkih.estore.payment.dto.request.InitializePaymentRequest;
import com.benkih.estore.payment.dto.response.InitializePaymentResponse;
import com.benkih.estore.payment.dto.response.PaymentResponse;
import com.benkih.estore.payment.dto.response.VerifyPaymentResponse;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.repository.OrderRepository;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.payment.provider.PaymentGateway;
import com.benkih.estore.payment.provider.PaymentGatewayFactory;
import com.benkih.estore.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultPaymentService implements IPaymentService {

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final PaymentGatewayFactory gatewayFactory;

  @Override
  public InitializePaymentResponse checkout(CheckoutRequest request) {

    Order order = orderRepository.findBySlug(request.getOrderSlug())
        .orElseThrow();

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
  public PaymentResponse verify(String reference) {

    Payment payment = paymentRepository.findByReference(reference)
        .orElseThrow();

    PaymentGateway gateway = gatewayFactory.get(payment.getPaymentProvider());
    VerifyPaymentResponse response = gateway.verify(reference);

    payment.setPaymentStatus(response.getStatus());
    payment.setTransactionId(response.getTransactionId());
    payment.setGatewayResponse(response.getGatewayResponse());
    payment.setPaidAt(response.getPaidAt());

    paymentRepository.save(payment);

    return convertToDto(payment);
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
}