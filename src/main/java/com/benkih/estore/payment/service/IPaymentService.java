package com.benkih.estore.payment.service;

import com.benkih.estore.payment.dto.request.CheckoutRequest;
import com.benkih.estore.payment.dto.response.InitializePaymentResponse;
import com.benkih.estore.payment.dto.response.PaymentResponse;
import com.benkih.estore.payment.entity.Payment;

public interface IPaymentService {

  InitializePaymentResponse checkout(CheckoutRequest request);

  PaymentResponse verify(String reference);

  PaymentResponse convertToDto(Payment payment);
}
