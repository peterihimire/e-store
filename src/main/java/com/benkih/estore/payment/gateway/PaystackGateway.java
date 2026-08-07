package com.benkih.estore.payment.gateway;

import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.payment.dto.request.InitializePaymentRequest;
import com.benkih.estore.payment.dto.request.RefundPaymentRequest;
import com.benkih.estore.payment.dto.response.InitializePaymentResponse;
import com.benkih.estore.payment.dto.response.RefundPaymentResponse;
import com.benkih.estore.payment.dto.response.VerifyPaymentResponse;
import com.benkih.estore.payment.provider.PaymentGateway;
import com.benkih.estore.vendor.PaystackClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaystackGateway implements PaymentGateway {
  private final PaystackClient paystackClient;

  @Override
  public PaymentProvider supports(){
    return PaymentProvider.PAYSTACK;
  }

  @Override
  public InitializePaymentResponse initialize(InitializePaymentRequest request){
    return paystackClient.initialize(request);
  }

  @Override
  public VerifyPaymentResponse verify(String reference){
    return paystackClient.verify(reference);
  }

  @Override
  public RefundPaymentResponse refund(RefundPaymentRequest request) {
    return paystackClient.refund(request);
  }

  @Override
  public RefundPaymentResponse verifyRefund(String refundReference) {
    return paystackClient.verifyRefund(refundReference);
  }

//  @Override
//  public WebhookEvent parseAndVerifyWebhook(String signature, String payload) {
//    return null;
//  }
}
