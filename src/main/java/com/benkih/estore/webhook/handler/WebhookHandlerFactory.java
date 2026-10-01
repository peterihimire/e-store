package com.benkih.estore.webhook.handler;

import com.benkih.estore.common.enums.PaymentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebhookHandlerFactory {
  private final PaystackWebhookHandler paystackWebhookHandler;

  public IWebhookHandler get(PaymentProvider provider) {

    return switch (provider) {
      case PAYSTACK -> paystackWebhookHandler;

      // case FLUTTERWAVE -> flutterwaveWebhookHandler;

      default -> throw new IllegalArgumentException(
          "Unsupported webhook provider: " + provider
      );
    };
  }
}
