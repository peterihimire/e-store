package com.benkih.estore.payment.dto.webhook;

import com.benkih.estore.common.enums.PaymentProvider;

import java.math.BigDecimal;

public interface PaymentWebhookEvent {
  PaymentProvider provider();

  String eventType();

  String reference();

  String transactionId();

  String transactionReference();

  BigDecimal amount();

  String refundReference();
}
