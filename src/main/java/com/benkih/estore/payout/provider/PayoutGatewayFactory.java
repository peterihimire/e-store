package com.benkih.estore.payout.provider;


import com.benkih.estore.payout.enums.PayoutProvider;
import com.benkih.estore.payout.gateway.PaystackPayoutGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayoutGatewayFactory {

  private final PaystackPayoutGateway paystackPayoutGateway;
//  private final FlutterwavePayoutGateway flutterwavePayoutGateway;

  public PayoutGateway get(PayoutProvider provider) {

    return switch (provider) {

      case PAYSTACK -> paystackPayoutGateway;

//      case FLUTTERWAVE -> flutterwavePayoutGateway;

      default -> throw new IllegalArgumentException(
          "Unsupported payout provider: " + provider
      );
    };
  }
}
