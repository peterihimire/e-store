package com.benkih.estore.order.repository;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.order.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
  @EntityGraph(attributePaths = {
      "user",
      "items",
      "items.product",
//      "items.product.images",
      "items.product.category"
  })
  Optional<Order> findBySlug(String slug);

  @EntityGraph(attributePaths = {
      "user",
      "items",
      "items.product",
//      "items.product.images",
      "items.product.category"
  })
  List<Order> findByUserSlug(String slug);
}
