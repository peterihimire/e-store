package com.benkih.estore.order.repository;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
  Optional<Order> findBySlug(String slug);

  List<Order> findByUserSlug(String slug);
}
