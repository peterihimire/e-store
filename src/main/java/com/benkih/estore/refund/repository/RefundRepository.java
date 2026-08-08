package com.benkih.estore.refund.repository;


import com.benkih.estore.refund.entity.Refund;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

  Optional<Refund> findBySlug(String slug);

  Optional<Refund> findByReference(String reference);

  Optional<Refund> findByPaymentReference(String paymentReference);

  List<Refund> findByUserOrderByCreatedAtDesc(User user);

  List<Refund> findByPaymentOrderOrderByCreatedAtDesc(Order order);

  boolean existsByPaymentIdAndRefundStatusIn(
      Long paymentId,
      List<com.benkih.estore.common.enums.RefundStatus> statuses
  );
}
