package com.benkih.estore.checkout.repository;

import com.benkih.estore.checkout.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
  Optional<Promotion> findByCodeIgnoreCase(
      String code
  );

  @Query("""
    SELECT p
    FROM Promotion p
    WHERE LOWER(p.code) = LOWER(:code)
      AND p.active = true
      AND p.startsAt <= :now
      AND (
          p.endsAt IS NULL
          OR p.endsAt >= :now
      )
    """)
  Optional<Promotion> findActive(
      @Param("code") String code,
      @Param("now") Instant now
  );

    @Query("""
      SELECT p
      FROM Promotion p
      WHERE UPPER(p.code) = UPPER(:code)
        AND p.active = true
        AND (p.startsAt IS NULL OR p.startsAt <= :now)
        AND (p.expiresAt IS NULL OR p.expiresAt >= :now)
      """)
    Optional<Promotion> findActiveForUpdate(
        @Param("code") String code,
        @Param("now") Instant now
    );

}
