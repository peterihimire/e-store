package com.benkih.estore.checkout.repository;

//package com.benkih.estore.tax.repository;

//import com.benkih.estore.tax.entity.TaxRule;
import com.benkih.estore.checkout.entity.TaxRule;
import com.benkih.estore.common.enums.TaxCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TaxRuleRepository
    extends JpaRepository<TaxRule, Long> {

  boolean existsByCode(String code);

  @Query("""
        SELECT r
        FROM TaxRule r
        WHERE r.jurisdiction = :jurisdiction
          AND r.taxCategory = :taxCategory
          AND r.active = true
          AND r.effectiveFrom <= :at
          AND (
              r.effectiveTo IS NULL
              OR r.effectiveTo > :at
          )
        ORDER BY r.effectiveFrom DESC
        """)
  Optional<TaxRule> findApplicableRule(
      @Param("jurisdiction") String jurisdiction,
      @Param("taxCategory") TaxCategory taxCategory,
      @Param("at") Instant at
  );
}
