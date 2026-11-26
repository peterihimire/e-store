package com.benkih.estore.payment.repository;

import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.payment.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReference(String reference);

    Optional<Payment> findBySlug(String slug);

    //  Optional<Payment> findByBusinessSlugAndReference(
    //      String businessSlug,
    //      String reference
    //  );
    //
    //  Optional<Payment> findByBusinessSlugAndSlug(
    //      String businessSlug,
    //      String slug
    //  );
    //
    //  Page<Payment> findByBusinessSlug(
    //      String businessSlug,
    //      Pageable pageable
    //  );

    Optional<Payment> findByOrderAndPaymentStatus(Order order, PaymentStatus status);

    Optional<Payment> findTopByOrderOrderByCreatedAtDesc(Order order);

    //  @Query("""
    //    SELECT COALESCE(SUM(p.amount), 0)
    //    FROM Payment p
    //    WHERE p.business.id = :businessId
    //    AND p.paymentStatus = :status
    // """)
    //  BigDecimal sumByBusinessSlugAndStatus(
    //      String businessSlug,
    //      PaymentStatus status
    //  );

}
