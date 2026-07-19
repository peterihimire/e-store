package com.benkih.estore.payment.dto.webhook;

import com.benkih.estore.common.enums.PaymentProvider;

public interface PaymentWebhookEvent {
  PaymentProvider provider();

  String eventType();

  String reference();

  String transactionId();
}
