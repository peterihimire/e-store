package com.benkih.estore.payment.provider;

import com.benkih.estore.common.enums.PaymentMethod;
import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.payment.dto.request.InitializePaymentRequest;
import com.benkih.estore.payment.dto.response.InitializePaymentResponse;
import com.benkih.estore.payment.dto.response.VerifyPaymentResponse;

public interface PaymentGateway {

  PaymentProvider supports();

  InitializePaymentResponse initialize(InitializePaymentRequest request);

  VerifyPaymentResponse verify(String reference);

//  void refund(String reference);
//
//  String getName();
}
