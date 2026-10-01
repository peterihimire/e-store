package com.benkih.estore.webhook.handler;

import com.benkih.estore.common.enums.PaymentProvider;

import java.math.BigDecimal;

public interface WebhookEvent {
  PaymentProvider provider();

  String eventType();

  String reference();

  String transactionId();

  String transactionReference();

  BigDecimal amount();

  String refundReference();
}
