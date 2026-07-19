package com.benkih.estore.payment.provider;


import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.payment.dto.response.VerifyPaymentResponse;
import com.benkih.estore.payment.dto.webhook.PaymentWebhookEvent;

public interface PaymentWebhookHandler {

  PaymentProvider supports();

  void verifySignature(String signature, String payload);

  PaymentWebhookEvent parseWebhook(String payload);

  VerifyPaymentResponse verify(String reference);
}