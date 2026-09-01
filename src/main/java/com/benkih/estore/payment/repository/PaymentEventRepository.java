package com.benkih.estore.payment.repository;

import com.benkih.estore.common.enums.PaymentEventType;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.payment.entity.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {
  //  Find events by payment, ordered by creation date
  List<PaymentEvent> findByPaymentOrderByCreatedAtDesc(Payment payment);

  //  Find events by payment and event type
  List<PaymentEvent> findByPaymentAndEventTypeOrderByCreatedAtDesc(Payment payment, PaymentEventType eventType);

  // Check if a payment has a specific event type
  boolean existsByPaymentAndEventType(Payment payment, PaymentEventType eventType);

  // Find webhook events by payment
  List<PaymentEvent> findByPaymentAndEventTypeInOrderByCreatedAtDesc(Payment payment, List<PaymentEventType> eventTypes);

  //  Find events older than a date (for cleanup)
  List<PaymentEvent> findByEventTypeAndCreatedAtBefore(PaymentEventType eventType, Instant dateTime);

}
