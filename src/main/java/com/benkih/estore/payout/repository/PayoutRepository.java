package com.benkih.estore.payout.repository;

import com.benkih.estore.payout.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayoutRepository extends JpaRepository<Payout, Long> {

  Optional<Payout> findBySlugAndBusinessId(
      String slug,
      Long businessId
  );

  List<Payout> findAllByBusinessIdOrderByCreatedAtDesc(
      Long businessId
  );

  boolean existsByIdempotencyKey(
      String idempotencyKey
  );
}
