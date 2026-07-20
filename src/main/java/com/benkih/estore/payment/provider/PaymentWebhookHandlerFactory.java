package com.benkih.estore.payment.provider;


import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.payment.gateway.PaystackWebhookHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentWebhookHandlerFactory {

  private final PaystackWebhookHandler paystackWebhookHandler;
//  private final FlutterwaveWebhookHandler flutterwaveWebhookHandler;

  public PaymentWebhookHandler get(PaymentProvider provider) {
    return switch (provider) {
      case PAYSTACK -> paystackWebhookHandler;
//      case FLUTTERWAVE -> flutterwaveWebhookHandler;
      default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
    };
  }
}