package com.benkih.estore.payout.repository;

import com.benkih.estore.payout.entity.Payout;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutRepository extends JpaRepository<Payout, Long> {

    Optional<Payout> findBySlugAndBusinessId(String slug, Long businessId);

    List<Payout> findAllByBusinessIdOrderByCreatedAtDesc(Long businessId);

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<Payout> findByIdempotencyKey(String idempotencyKey);

    Optional<Payout> findByProviderReference(String providerReference);
}
