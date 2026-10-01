package com.benkih.estore.webhook.handler;

import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.payment.dto.webhook.PaymentWebhookEvent;

public interface IWebhookHandler {
  PaymentProvider supports();

  void verifySignature(
      String signature,
      String payload
  );

  WebhookEvent parseWebhook(
      String payload
  );

//  PaymentWebhookEvent parseWebhook(
//      String payload
//  );
}

