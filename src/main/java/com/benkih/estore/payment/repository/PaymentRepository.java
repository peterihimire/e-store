package com.benkih.estore.payment.repository;

import com.benkih.estore.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository
    extends JpaRepository<Payment,Long> {

  Optional<Payment> findByReference(
      String reference);

  Optional<Payment> findBySlug(
      String slug);

}