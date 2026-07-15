package com.benkih.estore.order.service;

import com.benkih.estore.order.dto.response.OrderResponseDto;
import com.benkih.estore.order.entity.Order;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IOrderService {
  //  Order placeOrder(String orderSlug);
  OrderResponseDto placeOrder(String userSlug);

  Order getOrder(String orderSlug);

  @Transactional(readOnly = true)
  OrderResponseDto getOrderDtoBySlug(String slug);

  List<Order> getUserOrders(String slug);

  @Transactional(readOnly = true)
  List<OrderResponseDto> getConvertedOrders(List<Order> orders);

  OrderResponseDto convertToDto(Order order);
}
