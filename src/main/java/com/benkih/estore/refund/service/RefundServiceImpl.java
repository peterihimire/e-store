package com.benkih.estore.refund.service;

import com.benkih.estore.payment.dto.response.RefundPaymentResponse;
import com.benkih.estore.refund.dto.request.CreateRefundRequest;
import com.benkih.estore.refund.dto.response.RefundResponse;
import com.benkih.estore.refund.entity.Refund;
import com.benkih.estore.webhook.handler.WebhookEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {
    private final RefundApprovalService refundApprovalService;
    private final RefundGatewayService refundGatewayService;
    private final RefundQueryService refundQueryService;
    private final RefundSellerSettlementService refundSellerSettlementService;

    @Transactional
    @Override
    public RefundResponse requestRefund(CreateRefundRequest request) {
        return refundApprovalService.requestRefund(request);
    }

    @Transactional
    @Override
    public RefundPaymentResponse initializeRefund(String refundSlug) {
        return refundGatewayService.initializeRefund(refundSlug);
    }

    @Transactional
    @Override
    public RefundPaymentResponse verifyRefund(String refundReference) {
        return refundGatewayService.verifyRefund(refundReference);
    }

    // WEBHOOK STATUS UPDATES
    @Transactional
    @Override
    public void markPending(WebhookEvent event) {
        refundGatewayService.markPending(event);
    }

    @Transactional
    @Override
    public void markProcessing(WebhookEvent event) {
        refundGatewayService.markProcessing(event);
    }

    @Transactional
    @Override
    public void markNeedsAttention(WebhookEvent event) {
        refundGatewayService.markNeedsAttention(event);
    }

    @Transactional
    @Override
    public void markSuccessful(WebhookEvent event) {
        refundGatewayService.markSuccessful(event);
    }

    @Transactional
    @Override
    public void markFailed(WebhookEvent event, String reason) {
        refundGatewayService.markFailed(event, reason);
    }

    // RETRIEVAL
    @Transactional(readOnly = true)
    @Override
    public RefundResponse getRefund(String refundSlug) {
        return refundQueryService.getRefund(refundSlug);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RefundResponse> getUserRefunds(String userSlug) {
        return refundQueryService.getUserRefunds(userSlug);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RefundResponse> getOrderRefunds(String orderSlug) {
        return refundQueryService.getOrderRefunds(orderSlug);
    }

    // ADMIN
    @Transactional
    @Override
    public RefundResponse approveRefund(String refundSlug) {
        return refundApprovalService.approveRefund(refundSlug);
    }

    @Transactional
    @Override
    public RefundResponse rejectRefund(String refundSlug, String reason) {
        return refundApprovalService.rejectRefund(refundSlug, reason);
    }

    @Transactional
    @Override
    public void handleWebhook(WebhookEvent event, String signature, String payload) {
        refundGatewayService.handleWebhook(event, signature, payload);
    }

    // DTO
    @Override
    public RefundResponse convertToDto(Refund refund) {
        return refundQueryService.convertToDto(refund);
    }
}
