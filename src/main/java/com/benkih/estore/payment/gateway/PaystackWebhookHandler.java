package com.benkih.estore.payment.gateway;

import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.payment.dto.response.VerifyPaymentResponse;
import com.benkih.estore.payment.dto.webhook.PaymentWebhookEvent;
import com.benkih.estore.payment.provider.PaymentWebhookHandler;
import com.benkih.estore.vendor.PaystackClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaystackWebhookHandler implements PaymentWebhookHandler {
  private final PaystackClient paystackClient;

  @Override
  public PaymentProvider supports() {
    return PaymentProvider.PAYSTACK;
  }

  @Override
  public void verifySignature(String signature, String payload) {
    if (!paystackClient.verifyWebhookSignature(signature, payload)) {
      throw new IllegalArgumentException("Invalid webhook signature");
    }
  }

  @Override
  public PaymentWebhookEvent parseWebhook(String payload) {
    return paystackClient.parseWebhook(payload);
  }

  @Override
  public VerifyPaymentResponse verify(String reference) {
    return paystackClient.verify(reference);
  }
}
