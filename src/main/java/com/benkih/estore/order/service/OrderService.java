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

public interface OrderService {
    OrderResponseDto placeOrder(
            String userSlug,
            String couponCode,
            DeliveryMethod deliveryMethod,
            String shippingAddressSlug);

    Order getOrder(String slug);

    OrderResponseDto getOrderDtoBySlug(String slug);

    List<OrderResponseDto> getUserOrders();

    BusinessOrderResponseDto getBusinessOrder(String slug, Long businessId);

    List<BusinessOrderResponseDto> getBusinessOrders(Long businessId);

    BusinessOrderItemResponseDto getBusinessOrderItem(String slug, Long businessId);

    List<OrderResponseDto> getConvertedOrders(List<Order> orders);

    OrderResponseDto convertToDto(Order order);

    OrderItemProductResponseDto convertToOrderItemProductDto(OrderItem item);

    void processPaidOrder(Order order);

    void validateOrderCanBePaid(Order order);

    OrderResponseDto changeOrderStatus(String slug, OrderStatus status);

    void cancelOrder(Order order);

    void confirmOrder(Order order);

    void startProcessing(Order order);

    void shipOrder(Order order);

    void deliverOrder(Order order);

    void requestReturn(Order order);

    void completeReturn(Order order);
}
