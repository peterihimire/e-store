package com.benkih.estore.payment.gateway;

import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.payment.dto.request.InitializePaymentRequest;
import com.benkih.estore.payment.dto.response.InitializePaymentResponse;
import com.benkih.estore.payment.dto.response.VerifyPaymentResponse;
import com.benkih.estore.payment.provider.PaymentGateway;
import com.benkih.estore.vendor.PaystackClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaystackGateway implements PaymentGateway {
  private final PaystackClient client;

  @Override
  public PaymentProvider supports(){
    return PaymentProvider.PAYSTACK;
  }

  @Override
  public InitializePaymentResponse initialize(InitializePaymentRequest request){
    return client.initialize(request);
  }

  @Override
  public VerifyPaymentResponse verify(String reference){
    return client.verify(reference);
  }
}
