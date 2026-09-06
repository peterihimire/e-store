package com.benkih.estore.settlement.repository;

import com.benkih.estore.settlement.entity.Settlement;
import com.benkih.estore.settlement.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

  List<Settlement>
  findAllByBusinessIdOrderByCreatedAtDesc(
      Long businessId
  );

  Optional<Settlement>
  findBySlugAndBusinessId(
      String slug,
      Long businessId
  );

  List<Settlement>
  findAllByBusinessIdAndStatus(
      Long businessId,
      SettlementStatus status
  );
}
