package com.benkih.estore.order.service;

import com.benkih.estore.common.enums.DeliveryMethod;
import com.benkih.estore.common.enums.OrderStatus;
import com.benkih.estore.order.dto.response.BusinessOrderItemResponseDto;
import com.benkih.estore.order.dto.response.BusinessOrderResponseDto;
import com.benkih.estore.order.dto.response.OrderItemProductResponseDto;
import com.benkih.estore.order.dto.response.OrderResponseDto;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderPlacementService orderPlacementService;
    private final OrderQueryService orderQueryService;
    private final OrderStatusService orderStatusService;

    @Transactional
    @Override
    public OrderResponseDto placeOrder(
            String userSlug,
            String couponCode,
            DeliveryMethod deliveryMethod,
            String shippingAddressSlug) {
        return orderPlacementService.placeOrder(
                userSlug, couponCode, deliveryMethod, shippingAddressSlug);
    }

    @Transactional
    @Override
    public Order getOrder(String slug) {
        return orderQueryService.getOrder(slug);
    }

    @Transactional
    @Override
    public OrderResponseDto getOrderDtoBySlug(String slug) {
        return orderQueryService.getOrderDtoBySlug(slug);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderResponseDto> getUserOrders() {
        return orderQueryService.getUserOrders();
    }

    @Override
    public BusinessOrderResponseDto getBusinessOrder(String slug, Long businessId) {
        return orderQueryService.getBusinessOrder(slug, businessId);
    }

    @Override
    public List<BusinessOrderResponseDto> getBusinessOrders(Long businessId) {
        return orderQueryService.getBusinessOrders(businessId);
    }

    @Transactional(readOnly = true)
    @Override
    public BusinessOrderItemResponseDto getBusinessOrderItem(String slug, Long businessId) {
        return orderQueryService.getBusinessOrderItem(slug, businessId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderResponseDto> getConvertedOrders(List<Order> orders) {
        return orderQueryService.getConvertedOrders(orders);
    }

    @Transactional(readOnly = true)
    @Override
    public OrderResponseDto convertToDto(Order order) {
        return orderQueryService.convertToDto(order);
    }

    @Transactional(readOnly = true)
    @Override
    public OrderItemProductResponseDto convertToOrderItemProductDto(OrderItem item) {
        return orderQueryService.convertToOrderItemProductDto(item);
    }

    @Transactional
    @Override
    public void processPaidOrder(Order order) {
        orderStatusService.processPaidOrder(order);
    }

    @Transactional
    @Override
    public void validateOrderCanBePaid(Order order) {
        orderStatusService.validateOrderCanBePaid(order);
    }

    @Transactional
    @Override
    public OrderResponseDto changeOrderStatus(String slug, OrderStatus status) {
        return orderStatusService.changeOrderStatus(slug, status);
    }

    @Transactional
    @Override
    public void cancelOrder(Order order) {
        orderStatusService.cancelOrder(order);
    }

    @Transactional
    @Override
    public void confirmOrder(Order order) {
        orderStatusService.confirmOrder(order);
    }

    @Transactional
    @Override
    public void startProcessing(Order order) {
        orderStatusService.startProcessing(order);
    }

    @Transactional
    @Override
    public void shipOrder(Order order) {
        orderStatusService.shipOrder(order);
    }

    @Transactional
    @Override
    public void deliverOrder(Order order) {
        orderStatusService.deliverOrder(order);
    }

    @Transactional
    @Override
    public void requestReturn(Order order) {
        orderStatusService.requestReturn(order);
    }

    @Transactional
    @Override
    public void completeReturn(Order order) {
        orderStatusService.completeReturn(order);
    }
}
// NEXT PHASE
// things to work on : order status update by admin - PROCESSING - SHIPPED - DELIVERED - with EMAILS
// Work on order cancellation, refunds, failed payments
// work on AUTH, Logout, refresh token, forgot password, reset password
// Advanced Roles Based Access Control
// Invite users and assign roles
// AUDIT LOGS
// use AI to ask questions about product in question, compare product, what it can do and more - AI
// powered e-commerce platform
