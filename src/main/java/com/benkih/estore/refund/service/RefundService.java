package com.benkih.estore.refund.service;

import com.benkih.estore.payment.dto.response.RefundPaymentResponse;
import com.benkih.estore.refund.dto.request.CreateRefundRequest;
import com.benkih.estore.refund.dto.response.RefundResponse;
import com.benkih.estore.refund.entity.Refund;
import com.benkih.estore.webhook.handler.WebhookEvent;
import java.util.List;

public interface RefundService {
    RefundResponse requestRefund(CreateRefundRequest request);

    RefundPaymentResponse initializeRefund(String refundSlug);

    RefundPaymentResponse verifyRefund(String refundReference);

    void markPending(WebhookEvent event);

    void markProcessing(WebhookEvent event);

    void markNeedsAttention(WebhookEvent event);

    void markSuccessful(WebhookEvent event);

    void markFailed(WebhookEvent event, String reason);

    RefundResponse getRefund(String refundSlug);

    List<RefundResponse> getUserRefunds(String userSlug);

    List<RefundResponse> getOrderRefunds(String orderSlug);

    RefundResponse approveRefund(String refundSlug);

    RefundResponse rejectRefund(String refundSlug, String reason);

    void handleWebhook(WebhookEvent event, String signature, String payload);

    RefundResponse convertToDto(Refund refund);
}
