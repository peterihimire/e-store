package com.benkih.estore.order.repository;

import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderItemRepository  extends JpaRepository<OrderItem, Long> {

  Optional<OrderItem> findBySlugAndOrder(String slug, Order order);
}
