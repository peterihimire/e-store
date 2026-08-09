package com.benkih.estore.payment.provider;

import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.payment.gateway.PaystackGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class PaymentGatewayFactory { // with lombok, it automatically generates the  constructor behind the scene
  private final PaystackGateway paystackGateway;
//  private final StripeGateway stripeGateway;

  public PaymentGateway get(PaymentProvider provider) {
    return switch (provider) {
      case PAYSTACK -> paystackGateway;
      //      case STRIPE -> stripeGateway;
      default -> throw new IllegalArgumentException("Unsupported payment provider: " + provider);
    };
  }
}