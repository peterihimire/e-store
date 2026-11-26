package com.benkih.estore.refund.service;

import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.common.enums.RefundGatewayStatus;
import com.benkih.estore.common.enums.RefundStatus;
import com.benkih.estore.common.exceptions.ForbiddenException;
import com.benkih.estore.common.exceptions.PaymentException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import com.benkih.estore.order.repository.OrderItemRepository;
import com.benkih.estore.order.repository.OrderRepository;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.payment.repository.PaymentRepository;
import com.benkih.estore.refund.dto.request.CreateRefundRequest;
import com.benkih.estore.refund.dto.request.RefundItemRequest;
import com.benkih.estore.refund.dto.response.RefundResponse;
import com.benkih.estore.refund.entity.Refund;
import com.benkih.estore.refund.entity.RefundItem;
import com.benkih.estore.refund.repository.RefundItemRepository;
import com.benkih.estore.refund.repository.RefundRepository;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.user.entity.User;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundApprovalService {
    private final CurrentUserService currentUserService;
    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RefundItemRepository refundItemRepository;
    private final RefundQueryService refundQueryService;

    @Transactional
    public RefundResponse requestRefund(CreateRefundRequest request) {

        User user = currentUserService.getCurrentUser();
        Order order =
                orderRepository
                        .findBySlug(request.getOrderSlug())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not allowed to request a refund for this order.");
        }

        Payment payment =
                paymentRepository
                        .findByOrderAndPaymentStatus(order, PaymentStatus.SUCCESS)
                        .orElseThrow(
                                () ->
                                        new PaymentException(
                                                "This order does not have a successful payment."));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new PaymentException("At least one refund item is required.");
        }

        // Always acquire locks in a deterministic order
        List<RefundItemRequest> items =
                request.getItems().stream()
                        .sorted(Comparator.comparing(RefundItemRequest::getOrderItemSlug))
                        .toList();

        BigDecimal totalRefundAmount = BigDecimal.ZERO;

        List<RefundItem> refundItems = new ArrayList<>();

        for (RefundItemRequest itemRequest : items) {

            // Lock order-item
            OrderItem orderItem =
                    orderItemRepository
                            .findBySlugAndOrderForUpdate(itemRequest.getOrderItemSlug(), order)
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("Order item not found."));

            int requestedQuantity = itemRequest.getQuantity();

            if (requestedQuantity <= 0) {
                throw new PaymentException("Refund quantity must be greater than zero.");
            }

            validateRefundQuantity(orderItem, requestedQuantity);

            BigDecimal itemAmount =
                    orderItem.getPrice().multiply(BigDecimal.valueOf(requestedQuantity));

            RefundItem refundItem = new RefundItem();

            refundItem.setOrderItem(orderItem);
            refundItem.setQuantity(requestedQuantity);
            refundItem.setAmount(itemAmount);
            refundItems.add(refundItem);

            totalRefundAmount = totalRefundAmount.add(itemAmount);
        }

        if (totalRefundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Refund amount must be greater than zero.");
        }

        if (totalRefundAmount.compareTo(payment.getAmount()) > 0) {
            throw new PaymentException("Refund amount cannot exceed the payment amount.");
        }

        Refund refund = new Refund();

        refund.setUser(user);
        refund.setPayment(payment);
        refund.setAmount(totalRefundAmount);
        refund.setCurrency(payment.getCurrency());
        refund.setProvider(payment.getPaymentProvider());
        refund.setReference(generateRefundReference());
        refund.setReason(request.getReason());
        refund.setRefundStatus(RefundStatus.REQUESTED);
        refund.setGatewayStatus(RefundGatewayStatus.PENDING);

        for (RefundItem item : refundItems) {
            refund.addItem(item);
        }

        Refund saved = refundRepository.save(refund);

        log.info(
                "Refund {} requested for payment {} with amount {}",
                saved.getReference(),
                payment.getReference(),
                saved.getAmount());

        return refundQueryService.convertToDto(saved);
    }

    // ADMIN
    @Transactional
    public RefundResponse approveRefund(String refundSlug) {

        Refund refund = refundQueryService.getRefundEntity(refundSlug);

        if (refund.getRefundStatus() != RefundStatus.REQUESTED) {
            throw new PaymentException("Only requested refunds can be approved.");
        }

        refund.setRefundStatus(RefundStatus.APPROVED);
        Refund saved = refundRepository.save(refund);

        return refundQueryService.convertToDto(saved);
    }

    @Transactional
    public RefundResponse rejectRefund(String refundSlug, String reason) {

        Refund refund = refundQueryService.getRefundEntity(refundSlug);
        if (refund.getRefundStatus() != RefundStatus.REQUESTED) {
            throw new PaymentException("Only requested refunds can be rejected.");
        }
        refund.setRefundStatus(RefundStatus.REJECTED);
        refund.setFailureReason(reason);
        Refund saved = refundRepository.save(refund);

        return refundQueryService.convertToDto(saved);
    }

    // HELPERS
    private String generateRefundReference() {
        return "REF-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private void validateRefundQuantity(OrderItem orderItem, int requestedQuantity) {

        Integer activeQuantity =
                refundItemRepository.sumActiveRefundQuantityByOrderItemId(orderItem.getId());

        int activeRefundQuantity = activeQuantity != null ? activeQuantity : 0;

        int purchasedQuantity = orderItem.getQuantity();

        int availableQuantity = purchasedQuantity - activeRefundQuantity;

        if (requestedQuantity > availableQuantity) {
            throw new PaymentException(
                    String.format(
                            "Cannot refund %d unit(s). "
                                    + "Only %d unit(s) are available for refund.",
                            requestedQuantity, Math.max(availableQuantity, 0)));
        }
    }
}

//      if (requestedQuantity <= 0) {
//        throw new PaymentException("Refund quantity must be greater than zero.");
//      }
//
//      Integer refundedQuantity = refundItemRepository.sumRefundedQuantityByOrderItemId(
//              orderItem.getId(),
//              RefundGatewayStatus.PROCESSED
//          );
//
//      if (refundedQuantity == null) {
//        refundedQuantity = 0;
//      }
//
//      int remainingQuantity = orderItem.getQuantity() - refundedQuantity;
//
//      if (requestedQuantity > remainingQuantity) {
//        throw new PaymentException(
//            "Requested refund quantity exceeds the " + "remaining refundable quantity."
//        );
//      }

//    BigDecimal refundAmount = request.getAmount() != null
//        ? request.getAmount()
//        : payment.getAmount();

//    if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
//      throw new PaymentException("Refund amount must be greater than zero.");
//    }
//
//    if (refundAmount.compareTo(payment.getAmount()) > 0) {
//      throw new PaymentException("Refund amount cannot exceed the payment amount.");
//
//    }
