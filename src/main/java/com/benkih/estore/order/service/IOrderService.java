package com.benkih.estore.order.service;

import com.benkih.estore.order.entity.Order;

import java.util.List;

public interface IOrderService {
  Order placeOrder(String orderSlug);
  Order getOrder(String orderSlug);

  List<Order> getUserOrders(String slug);
}
