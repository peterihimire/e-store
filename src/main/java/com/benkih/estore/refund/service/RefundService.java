package com.benkih.estore.refund.service;


import com.benkih.estore.audit.service.ApiLogService;
import com.benkih.estore.notification.INotificationService;
import com.benkih.estore.payment.dto.response.RefundPaymentResponse;
import com.benkih.estore.payment.provider.PaymentGatewayFactory;
import com.benkih.estore.refund.dto.request.CreateRefundRequest;
import com.benkih.estore.refund.dto.response.RefundResponse;
import com.benkih.estore.refund.entity.Refund;
import com.benkih.estore.security.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService implements IRefundService{
  private final CurrentUserService currentUserService;
  private final ApiLogService apiLogService;
  private final INotificationService notificationService;
  private final PaymentGatewayFactory gatewayFactory;


  @Override
  public RefundResponse requestRefund(CreateRefundRequest request) {
    return null;
  }

  @Override
  public RefundPaymentResponse initializeRefund(String refundSlug) {
    return null;
  }

  @Override
  public RefundPaymentResponse verifyRefund(String refundReference) {
    return null;
  }

  @Override
  public void markPending(String refundReference) {

  }

  @Override
  public void markProcessing(String refundReference) {

  }

  @Override
  public void markNeedsAttention(String refundReference) {

  }

  @Override
  public void markFailed(String refundReference, String reason) {

  }

  @Override
  public void markSuccessful(String refundReference) {

  }

  @Override
  public RefundResponse getRefund(String refundSlug) {
    return null;
  }

  @Override
  public List<RefundResponse> getUserRefunds(String userSlug) {
    return List.of();
  }

  @Override
  public List<RefundResponse> getOrderRefunds(String orderSlug) {
    return List.of();
  }

  @Override
  public RefundResponse approveRefund(String refundSlug) {
    return null;
  }

  @Override
  public RefundResponse rejectRefund(String refundSlug, String reason) {
    return null;
  }

  @Override
  public RefundResponse convertToDto(Refund refund) {
    return null;
  }
}
