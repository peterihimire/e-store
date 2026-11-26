package com.benkih.estore.refund.service;

import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.repository.OrderRepository;
import com.benkih.estore.refund.dto.response.RefundItemResponse;
import com.benkih.estore.refund.dto.response.RefundResponse;
import com.benkih.estore.refund.entity.Refund;
import com.benkih.estore.refund.entity.RefundItem;
import com.benkih.estore.refund.repository.RefundRepository;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundQueryService {
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public RefundResponse getRefund(String refundSlug) {
        Refund refund = getRefundEntity(refundSlug);

        return convertToDto(refund);
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> getUserRefunds(String userSlug) {
        User user = currentUserService.getCurrentUser();

        return refundRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> getOrderRefunds(String orderSlug) {

        Order order =
                orderRepository
                        .findBySlug(orderSlug)
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return refundRepository.findByPaymentOrderOrderByCreatedAtDesc(order).stream()
                .map(this::convertToDto)
                .toList();
    }

    // DTO
    public RefundResponse convertToDto(Refund refund) {
        return RefundResponse.builder()
                .slug(refund.getSlug())
                .reference(refund.getReference())
                .orderSlug(refund.getPayment().getOrder().getSlug())
                .paymentReference(refund.getPayment().getReference())
                .gatewayRefundId(refund.getGatewayRefundId())
                .amount(refund.getAmount())
                .currency(refund.getCurrency())
                .provider(refund.getProvider())
                .refundStatus(refund.getRefundStatus())
                .gatewayStatus(refund.getGatewayStatus())
                .reason(refund.getReason())
                .failureReason(refund.getFailureReason())
                .refundedAt(refund.getRefundedAt())
                .createdAt(refund.getCreatedAt())
                .items(refund.getItems().stream().map(this::convertRefundItemToDto).toList())
                .build();
    }

    // ENTITY LOOKUP HELPERS
    public Refund getRefundEntity(String refundSlug) {
        return refundRepository
                .findBySlug(refundSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found"));
    }

    public Refund getRefundByReference(String reference) {
        return refundRepository
                .findByReference(reference)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Refund not found for reference: " + reference));
    }

    public Refund getRefundByPaymentReference(String paymentReference) {
        return refundRepository
                .findByPaymentReference(paymentReference)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Refund not found for payment reference: "
                                                + paymentReference));
    }

    private RefundItemResponse convertRefundItemToDto(RefundItem item) {

        return RefundItemResponse.builder()
                .slug(item.getSlug())
                .orderItemSlug(item.getOrderItem().getSlug())
                .quantity(item.getQuantity())
                .amount(item.getAmount())
                .build();
    }
}
