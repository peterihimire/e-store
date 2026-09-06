package com.benkih.estore.ledger.repository;

import com.benkih.estore.ledger.entity.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, Long> {

  boolean existsByReference(String reference);

  Optional<LedgerTransaction>
  findByReference(String reference);
}
