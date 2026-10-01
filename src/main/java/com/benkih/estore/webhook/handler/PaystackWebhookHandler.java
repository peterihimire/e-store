package com.benkih.estore.webhook.handler;

import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.payment.dto.webhook.PaymentWebhookEvent;
import com.benkih.estore.vendor.PaystackClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaystackWebhookHandler implements IWebhookHandler {
  private final PaystackClient paystackClient;

  @Override
  public PaymentProvider supports() {
    return PaymentProvider.PAYSTACK;
  }

  @Override
  public void verifySignature(
      String signature,
      String payload
  ) {
    if (!paystackClient.verifyWebhookSignature(signature, payload)) {
      throw new IllegalArgumentException(
          "Invalid Paystack webhook signature"
      );
    }
  }

  @Override
  public WebhookEvent parseWebhook(String payload) {
    return paystackClient.parseWebhook(payload);
  }

//  @Override
//  public PaymentWebhookEvent parseWebhook(
//      String payload
//  ) {
//    return paystackClient.parseWebhook(payload);
//  }

}
