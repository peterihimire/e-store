package com.benkih.estore.refund.repository;


import com.benkih.estore.common.enums.RefundGatewayStatus;
import com.benkih.estore.product.entity.Category;
import com.benkih.estore.refund.entity.Refund;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

  Optional<Refund> findBySlug(String slug);

  Optional<Refund> findByBusinessIdAndSlug(String businessId, String slug);

  Optional<Refund> findByReference(String reference);

  Optional<Refund> findByPaymentReference(String paymentReference);

  Optional<Refund> findByPaymentBusinessIdAndReference(
      Long businessId,
      String reference
  );

  List<Refund> findByUserOrderByCreatedAtDesc(User user);

  List<Refund> findByPaymentOrderOrderByCreatedAtDesc(Order order);

  boolean existsByPaymentIdAndRefundStatusIn(
      Long paymentId,
      List<com.benkih.estore.common.enums.RefundStatus> statuses
  );

  Optional<Refund> findByPaymentBusinessIdAndPaymentReference(
      Long businessId,
      String paymentReference
  );

  @Query("""
    SELECT COALESCE(SUM(r.amount), 0)
    FROM Refund r
    WHERE r.payment.id = :paymentId
      AND r.gatewayStatus = :status
""")
  BigDecimal sumRefundsByPaymentId(
      @Param("paymentId") Long paymentId,
      @Param("status") RefundGatewayStatus status
  );
}
