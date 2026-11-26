package com.benkih.estore.ledger.repository;

import com.benkih.estore.ledger.entity.LedgerTransaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, Long> {

    boolean existsByReference(String reference);

    Optional<LedgerTransaction> findByReference(String reference);
}
