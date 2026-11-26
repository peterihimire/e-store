package com.benkih.estore.ledger.repository;

import com.benkih.estore.ledger.entity.LedgerEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findAllByAccountIdOrderByCreatedAtAsc(Long accountId);

    List<LedgerEntry> findAllByBusinessIdOrderByCreatedAtAsc(Long businessId);

    List<LedgerEntry> findAllBySettlementId(Long settlementId);

    List<LedgerEntry> findAllByPayoutId(Long payoutId);
}
