package com.benkih.estore.order.service;

import com.benkih.estore.common.enums.DeliveryMethod;
import com.benkih.estore.common.enums.OrderStatus;
import com.benkih.estore.order.dto.response.BusinessOrderItemResponseDto;
import com.benkih.estore.order.dto.response.BusinessOrderResponseDto;
import com.benkih.estore.order.dto.response.OrderResponseDto;
import com.benkih.estore.order.entity.Order;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IOrderService {
  //  Order placeOrder(String orderSlug);
  OrderResponseDto placeOrder(String userSlug, String couponCode, DeliveryMethod deliveryMethod, String shippingAddressSlug);

  Order getOrder(String orderSlug);

  @Transactional(readOnly = true)
  OrderResponseDto getOrderDtoBySlug(String slug);

  List<Order> getUserOrders();

  @Transactional(readOnly = true)
  List<OrderResponseDto> getConvertedOrders(List<Order> orders);

  OrderResponseDto convertToDto(Order order);

  @Transactional
  OrderResponseDto changeOrderStatus(String slug, OrderStatus status);

  BusinessOrderItemResponseDto getBusinessOrderItem(String slug,
                                                     Long businessId);

  BusinessOrderResponseDto getBusinessOrder(String slug, Long businessId);

  List<BusinessOrderResponseDto> getBusinessOrders(Long businessId);


}
