package com.benkih.estore.order.repository;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
  @EntityGraph(attributePaths = {
      "user",
      "orderItems",
      "orderItems.product",
//      "items.product.images",
      "orderItems.product.category"
  })
  Optional<Order> findBySlug(String slug);

  @EntityGraph(attributePaths = {
      "user",
      "orderItems",
      "orderItems.product",
//      "items.product.images",
      "orderItems.product.category"
  })
  Optional<Order> findByBusinessIdAndSlug(
      Long businessId,
      String slug
  );

  @EntityGraph(attributePaths = {
      "user",
      "orderItems",
      "orderItems.product",
//      "items.product.images",
      "orderItems.product.category"
  })
  Optional<Order> findByBusinessIdAndOrderNumber(
      Long businessId,
      String orderNumber
  );

  @EntityGraph(attributePaths = {
      "user",
      "orderItems",
      "orderItems.product",
//      "items.product.images",
      "orderItems.product.category"
  })
  Page<Order> findByBusinessId(
      Long businessId,
      Pageable pageable
  );

  @EntityGraph(attributePaths = {
      "user",
      "orderItems",
      "orderItems.product",
//      "items.product.images",
      "orderItems.product.category"
  })
  List<Order> findByUserSlug(String slug);
}
