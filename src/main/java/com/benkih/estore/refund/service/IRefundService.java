package com.benkih.estore.refund.service;

import com.benkih.estore.payment.dto.response.RefundPaymentResponse;
import com.benkih.estore.payment.dto.webhook.PaymentWebhookEvent;
import com.benkih.estore.refund.dto.request.CreateRefundRequest;
import com.benkih.estore.refund.dto.response.RefundResponse;
import com.benkih.estore.refund.entity.Refund;

import java.util.List;

public interface IRefundService {

  // Customer/Admin requests a refund
  RefundResponse requestRefund(CreateRefundRequest request);

  // Sends the refund request to the payment gateway
  RefundPaymentResponse initializeRefund(String refundSlug);

  // Verify refund status from gateway
  RefundPaymentResponse verifyRefund(String refundReference);

  // Webhook status updates
  void markPending(PaymentWebhookEvent event);

  void markProcessing(PaymentWebhookEvent event);

  void markNeedsAttention(PaymentWebhookEvent event);

  void markFailed(PaymentWebhookEvent event, String reason);

  void markSuccessful(PaymentWebhookEvent event);

  // Retrieval
  RefundResponse getRefund(String refundSlug);

  List<RefundResponse> getUserRefunds(String userSlug);

  List<RefundResponse> getOrderRefunds(String orderSlug);

  // Admin
  RefundResponse approveRefund(String refundSlug);

  RefundResponse rejectRefund(String refundSlug, String reason);

  // DTO conversion
  RefundResponse convertToDto(Refund refund);
}
