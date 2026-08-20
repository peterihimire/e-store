package com.benkih.estore.order.repository;

import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
