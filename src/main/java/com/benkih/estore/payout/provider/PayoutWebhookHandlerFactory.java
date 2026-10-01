//package com.benkih.estore.payout.provider;
//
//
//import com.benkih.estore.payout.enums.PayoutProvider;
//import com.benkih.estore.payout.gateway.PaystackPayoutWebhookHandler;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class PayoutWebhookHandlerFactory {
//  private final PaystackPayoutWebhookHandler paystackPayoutWebhookHandler;
////  private final FlutterwavePayoutWebhookHandler flutterwavePayoutWebhookHandler;
//
//  public PayoutWebhookHandler get(PayoutProvider provider) {
//    return switch (provider) {
//      case PAYSTACK -> paystackPayoutWebhookHandler;
////      case FLUTTERWAVE -> flutterwavePayoutWebhookHandler;
//
//      default -> throw new IllegalArgumentException(
//          "Unsupported payout provider: " + provider
//      );
//    };
//  }
//}
