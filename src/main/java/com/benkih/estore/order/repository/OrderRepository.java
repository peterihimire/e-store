package com.benkih.estore.order.repository;

import com.benkih.estore.common.enums.OrderStatus;
import com.benkih.estore.order.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(
            attributePaths = {
                "user",
                "orderItems",
                "orderItems.product",
                "orderItems.product.category"
            })
    Optional<Order> findBySlug(String slug);

    //  @EntityGraph(attributePaths = {
    //      "user",
    //      "orderItems",
    //      "orderItems.product",
    //      "orderItems.product.category"
    //  })
    //  Optional<Order> findByBusinessSlugAndSlug(
    //      String businessSlug,
    //      String slug
    //  );

    //  @EntityGraph(attributePaths = {
    //      "user",
    //      "orderItems",
    //      "orderItems.product",
    //      "orderItems.product.category"
    //  })
    //  Optional<Order> findByBusinessSlugAndOrderNumber(
    //      String businessSlug,
    //      String orderNumber
    //  );

    //  @EntityGraph(attributePaths = {
    //      "user",
    //      "orderItems",
    //      "orderItems.product",
    //      "orderItems.product.category"
    //  })
    //  Page<Order> findByBusinessSlug(
    //      String businessSlug,
    //      Pageable pageable
    //  );

    @EntityGraph(
            attributePaths = {
                "user",
                "orderItems",
                "orderItems.product",
                "orderItems.product.category"
            })
    List<Order> findByUserSlug(String slug);

    boolean existsByUserSlugAndOrderItemsProductSlugAndOrderStatus(
            String userSlug, String productSlug, OrderStatus status);
}
