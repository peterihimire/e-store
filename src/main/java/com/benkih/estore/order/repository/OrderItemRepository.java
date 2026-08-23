package com.benkih.estore.order.repository;

import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository  extends JpaRepository<OrderItem, Long> {

  Optional<OrderItem> findBySlugAndOrder(String slug, Order order);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
    SELECT oi
    FROM OrderItem oi
    WHERE oi.id = :id
""")
  Optional<OrderItem> findByIdForUpdate(
      @Param("id") Long id
  );

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
    SELECT oi
    FROM OrderItem oi
    WHERE oi.slug = :slug
      AND oi.order = :order
""")
  Optional<OrderItem> findBySlugAndOrderForUpdate(
      @Param("slug") String slug,
      @Param("order") Order order
  );

  @Query("""
        SELECT DISTINCT oi
        FROM OrderItem oi
        JOIN FETCH oi.order o
        JOIN FETCH oi.product p
        LEFT JOIN FETCH p.images
        LEFT JOIN FETCH p.category
        WHERE oi.business.slug = :businessSlug
        ORDER BY o.orderDate DESC
    """)
  List<OrderItem> findBusinessOrderItems(
      @Param("businessSlug") String businessSlug
  );

  @Query("""
        SELECT oi
        FROM OrderItem oi
        JOIN FETCH oi.order o
        JOIN FETCH oi.product p
        LEFT JOIN FETCH p.images
        LEFT JOIN FETCH p.category
        WHERE oi.slug = :orderItemSlug
        AND oi.business.slug = :businessSlug
    """)
  Optional<OrderItem> findBusinessOrderItem(
      @Param("orderItemSlug") String orderItemSlug,
      @Param("businessSlug") String businessSlug
  );

  @Query("""
    SELECT oi
    FROM OrderItem oi
    JOIN FETCH oi.order o
    JOIN FETCH oi.product p
    LEFT JOIN FETCH p.images
    LEFT JOIN FETCH p.category
    WHERE o.slug = :orderSlug
    AND oi.business.slug = :businessSlug
""")
  List<OrderItem> findBusinessOrderItemsByOrderSlug(
      @Param("orderSlug") String orderSlug,
      @Param("businessSlug") String businessSlug
  );
}
