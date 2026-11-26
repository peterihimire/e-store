package com.benkih.estore.payment.service;

import com.benkih.estore.payment.dto.request.CheckoutRequest;
import com.benkih.estore.payment.dto.response.InitializePaymentResponse;
import com.benkih.estore.payment.dto.response.PaymentResponse;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.webhook.handler.WebhookEvent;

public interface PaymentService {
    InitializePaymentResponse checkout(CheckoutRequest request);

    PaymentResponse verify(String reference);

    void handleWebhook(WebhookEvent event, String signature, String payload);

    PaymentResponse convertToDto(Payment payment);
}
