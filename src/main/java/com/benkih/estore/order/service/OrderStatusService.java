package com.benkih.estore.order.service;

import com.benkih.estore.common.enums.OrderStatus;
import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.common.exceptions.DuplicatePaymentException;
import com.benkih.estore.common.exceptions.PaymentException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.inventory.service.InventoryService;
import com.benkih.estore.notification.NotificationService;
import com.benkih.estore.order.dto.response.OrderResponseDto;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import com.benkih.estore.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusService {
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final OrderQueryService orderQueryService;

    @Transactional
    public OrderResponseDto changeOrderStatus(String slug, OrderStatus status) {
        Order order =
                orderRepository
                        .findBySlug(slug)
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        switch (status) {
            case CONFIRMED -> confirmOrder(order);
            case PROCESSING -> startProcessing(order);
            case SHIPPED -> shipOrder(order);
            case DELIVERED -> deliverOrder(order);
            case RETURN_REQUESTED -> requestReturn(order);
            case RETURNED -> completeReturn(order);
            case CANCELLED -> cancelOrder(order);
            default -> throw new IllegalArgumentException("Unsupported status: " + status);
        }
        return orderQueryService.convertToDto(order);
    }

    @Transactional
    public void cancelOrder(Order order) {
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            return;
        }

        if (order.getOrderStatus() != OrderStatus.PENDING
                && order.getOrderStatus() != OrderStatus.CONFIRMED) {

            throw new IllegalStateException("Order cannot be cancelled.");
        }

        for (OrderItem item : order.getOrderItems()) {
            inventoryService.release(item.getProduct().getSlug(), item.getQuantity());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        notificationService.sendOrderCancelled(order);
    }

    @Transactional
    public void confirmOrder(Order order) {
        updateOrderStatus(order, OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    @Transactional
    public void startProcessing(Order order) {
        updateOrderStatus(order, OrderStatus.PROCESSING);
        orderRepository.save(order);
        notificationService.sendOrderProcessing(order);
    }

    @Transactional
    public void shipOrder(Order order) {
        updateOrderStatus(order, OrderStatus.SHIPPED);
        orderRepository.save(order);
        notificationService.sendOrderShipped(order);
        //    trackingService.createTrackingNumber();
        //    notificationService.sendShipmentEmail();
        //    eventPublisher.publish(...);
    }

    @Transactional
    public void deliverOrder(Order order) {
        updateOrderStatus(order, OrderStatus.DELIVERED);
        orderRepository.save(order);
        notificationService.sendOrderDelivered(order);
    }

    @Transactional
    public void requestReturn(Order order) {
        updateOrderStatus(order, OrderStatus.RETURN_REQUESTED);
        orderRepository.save(order);
    }

    @Transactional
    public void completeReturn(Order order) {
        updateOrderStatus(order, OrderStatus.RETURNED);

        for (OrderItem item : order.getOrderItems()) {
            inventoryService.addStock(item.getProduct().getSlug(), item.getQuantity());
        }

        orderRepository.save(order);
    }

    private void updateOrderStatus(Order order, OrderStatus newStatus) {
        if (!isValidTransition(order.getOrderStatus(), newStatus)) {
            throw new IllegalStateException(
                    "Cannot change order status from "
                            + order.getOrderStatus()
                            + " to "
                            + newStatus);
        }

        order.setOrderStatus(newStatus);
    }

    private boolean isValidTransition(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case PENDING ->
                    next == OrderStatus.CONFIRMED
                            || next == OrderStatus.CANCELLED
                            || next == OrderStatus.EXPIRED;
            case CONFIRMED -> next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED;
            case PROCESSING -> next == OrderStatus.SHIPPED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED -> next == OrderStatus.RETURN_REQUESTED;
            case RETURN_REQUESTED -> next == OrderStatus.RETURNED;
            case RETURNED, CANCELLED, EXPIRED -> false;
        };
    }

    @Transactional
    public void processPaidOrder(Order order) {
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.CONFIRMED);

        for (OrderItem item : order.getOrderItems()) {
            inventoryService.fulfillReservation(item.getVariant().getSlug(), item.getQuantity());
        }
    }

    @Transactional
    public void validateOrderCanBePaid(Order order) {
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.info("Order validate...={}", order.getSlug());
            throw new DuplicatePaymentException(
                    String.format("Order %s has already been paid for.", order.getSlug()));
        }

        if (order.getOrderStatus() == OrderStatus.PROCESSING
                || order.getOrderStatus() == OrderStatus.SHIPPED
                || order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new PaymentException(
                    String.format(
                            "Order %s is already being processed and cannot be paid for.",
                            order.getSlug()));
        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new PaymentException(
                    String.format("Order %s has been cancelled.", order.getSlug()));
        }

        if (order.getOrderStatus() == OrderStatus.EXPIRED) {
            throw new PaymentException(String.format("Order %s has expired.", order.getSlug()));
        }
    }
}
