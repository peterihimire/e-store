package com.benkih.estore.refund.repository;

import com.benkih.estore.common.enums.RefundGatewayStatus;
import com.benkih.estore.refund.entity.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundItemRepository extends JpaRepository<RefundItem, Long> {
  @Query("""
        SELECT COALESCE(SUM(ri.quantity), 0)
        FROM RefundItem ri
        WHERE ri.orderItem.id = :orderItemId
          AND ri.refund.gatewayStatus = :status
    """)

  Integer sumRefundedQuantityByOrderItemId(
      @Param("orderItemId") Long orderItemId,
      @Param("status") RefundGatewayStatus status
  );
}
