package com.benkih.estore.refund.service;

import com.benkih.estore.payment.dto.response.RefundPaymentResponse;
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
  void markPending(String refundReference);

  void markProcessing(String refundReference);

  void markNeedsAttention(String refundReference);

  void markFailed(String refundReference, String reason);

  void markSuccessful(String refundReference);

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
