package com.benkih.estore.allocation.repository;

import com.benkih.estore.allocation.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
  boolean existsByPaymentId(Long paymentId);

  List<Allocation> findByPaymentId(Long paymentId);

  @Query("""
    SELECT a
    FROM Allocation a
    JOIN a.orderItem oi
    JOIN oi.order o
    WHERE o.orderStatus = com.benkih.estore.common.enums.OrderStatus.DELIVERED
      AND o.deliveredAt IS NOT NULL
      AND o.deliveredAt <= :eligibleBefore
      AND NOT EXISTS (
          SELECT si
          FROM SettlementItem si
          WHERE si.allocation = a
      )
""")
  List<Allocation> findEligibleForSettlement(
      @Param("eligibleBefore") Instant eligibleBefore
  );

  @Query("""
    SELECT a
    FROM Allocation a
    JOIN a.orderItem oi
    JOIN oi.order o
    WHERE o.orderStatus =
        com.benkih.estore.common.enums.OrderStatus.DELIVERED
      AND o.deliveredAt IS NOT NULL
      AND NOT EXISTS (
          SELECT si
          FROM SettlementItem si
          WHERE si.allocation = a
      )
""")
  List<Allocation> findDeliveredAndUnsettled();
}
