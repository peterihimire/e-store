package com.benkih.estore.order.service;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.business.repository.BusinessRepository;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.order.dto.response.BusinessOrderItemResponseDto;
import com.benkih.estore.order.dto.response.BusinessOrderResponseDto;
import com.benkih.estore.order.dto.response.OrderItemProductResponseDto;
import com.benkih.estore.order.dto.response.OrderItemResponseDto;
import com.benkih.estore.order.dto.response.OrderResponseDto;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import com.benkih.estore.order.repository.OrderItemRepository;
import com.benkih.estore.order.repository.OrderRepository;
import com.benkih.estore.product.dto.response.VariantAttributeResponseDto;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.entity.ProductVariant;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.user.entity.User;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderQueryService {
    private final OrderRepository orderRepository;
    private final BusinessRepository businessRepository;
    private final OrderItemRepository orderItemRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public Order getOrder(String slug) {
        Order order =
                orderRepository
                        .findBySlug(slug)
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return order;
    }

    @Transactional
    public OrderResponseDto getOrderDtoBySlug(String slug) {
        Order order =
                orderRepository
                        .findBySlug(slug)
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return convertToDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getUserOrders() {
        User user = currentUserService.getCurrentUser();
        //    return orderRepository.findByUserSlug(user.getSlug());
        List<Order> orders = orderRepository.findByUserSlug(user.getSlug());

        //    orders.forEach(order ->
        //        order.getOrderItems().forEach(item ->
        //            item.getProduct().getImages().size()
        //        )
        //    );

        //    return orders;
        return orders.stream().map(this::convertToDto).toList();
    }

    public BusinessOrderResponseDto getBusinessOrder(String slug, Long businessId) {

        Business business =
                businessRepository
                        .findById(businessId)
                        .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        List<OrderItem> businessItems =
                orderItemRepository.findBusinessOrderItemsByOrderSlug(slug, business.getSlug());

        if (businessItems.isEmpty()) {
            throw new ResourceNotFoundException("Order not found");
        }

        Order order = businessItems.get(0).getOrder();

        return convertBusinessOrderToDto(order, businessItems);
    }

    public List<BusinessOrderResponseDto> getBusinessOrders(Long businessId) {
        Business business =
                businessRepository
                        .findById(businessId)
                        .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        List<OrderItem> orderItems = orderItemRepository.findBusinessOrderItems(business.getSlug());

        Map<Order, List<OrderItem>> groupedOrders =
                orderItems.stream()
                        .collect(
                                Collectors.groupingBy(
                                        OrderItem::getOrder,
                                        LinkedHashMap::new,
                                        Collectors.toList()));

        return groupedOrders.entrySet().stream()
                .map(entry -> convertBusinessOrderToDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public BusinessOrderItemResponseDto getBusinessOrderItem(String slug, Long businessId) {

        Business business =
                businessRepository
                        .findById(businessId)
                        .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        OrderItem orderItem =
                orderItemRepository
                        .findBusinessOrderItem(slug, business.getSlug())
                        .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        return convertBusinessOrderItemToDto(orderItem);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getConvertedOrders(List<Order> orders) {
        return orders.stream().map(this::convertToDto).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponseDto convertToDto(Order order) {

        List<String> productSlugs =
                order.getOrderItems().stream().map(item -> item.getProduct().getSlug()).toList();

        List<OrderItemResponseDto> items =
                order.getOrderItems().stream()
                        .map(
                                item -> {
                                    Product product = item.getProduct();
                                    ProductVariant variant = item.getVariant();

                                    return new OrderItemResponseDto(
                                            item.getSlug(),
                                            convertToOrderItemProductDto(item),
                                            item.getVariant().getSlug(),
                                            item.getSku(),
                                            variant.getAttributes().stream()
                                                    .map(
                                                            attribute ->
                                                                    new VariantAttributeResponseDto(
                                                                            attribute
                                                                                    .getAttribute()
                                                                                    .getName(),
                                                                            attribute
                                                                                    .getAttributeValue()
                                                                                    .getValue()))
                                                    .toList(),
                                            item.getQuantity(),
                                            item.getPrice(),
                                            item.getSubtotal(),
                                            item.getDiscountAmount(),
                                            item.getTaxRate(),
                                            item.getTaxAmount());
                                })
                        .toList();

        return new OrderResponseDto(
                order.getSlug(),
                order.getUser().getSlug(),
                order.getOrderDate(),
                order.getCurrency(),
                order.getSubTotal(),
                order.getDiscountAmount(),
                order.getTaxAmount(),
                order.getShippingFee(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                items);
    }

    private BusinessOrderItemResponseDto convertBusinessOrderItemToDto(OrderItem item) {
        Order order = item.getOrder();

        BigDecimal total = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        return new BusinessOrderItemResponseDto(
                item.getSlug(),
                order.getSlug(),
                order.getOrderNumber(),
                item.getProduct().getSlug(),
                item.getName(),
                item.getSku(),
                item.getBrand(),
                item.getQuantity(),
                item.getPrice(),
                total,
                order.getOrderStatus().name(),
                order.getPaymentStatus().name(),
                order.getOrderDate());
    }

    private BusinessOrderResponseDto convertBusinessOrderToDto(
            Order order, List<OrderItem> businessItems) {
        List<BusinessOrderItemResponseDto> items =
                businessItems.stream().map(this::convertBusinessOrderItemToDto).toList();

        BigDecimal subtotal =
                businessItems.stream()
                        .map(
                                item ->
                                        item.getPrice()
                                                .multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BusinessOrderResponseDto(
                order.getSlug(),
                order.getOrderNumber(),
                order.getOrderDate(),
                order.getOrderStatus().name(),
                order.getPaymentStatus().name(),
                items,
                subtotal,
                subtotal);
    }

    @Transactional(readOnly = true)
    public OrderItemProductResponseDto convertToOrderItemProductDto(OrderItem item) {
        Product product = item.getProduct();

        return new OrderItemProductResponseDto(
                product.getSlug(),
                product.getName(),
                product.getBrand() != null ? product.getBrand().getName() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getImages().isEmpty() ? null : product.getImages().get(0).getDownloadUrl());
    }
}
