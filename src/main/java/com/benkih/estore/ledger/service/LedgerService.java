package com.benkih.estore.ledger.service;

import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.ledger.dto.LedgerPosting;
import com.benkih.estore.ledger.entity.LedgerEntry;
import com.benkih.estore.ledger.entity.LedgerTransaction;
import com.benkih.estore.ledger.enums.LedgerEntryDirection;
import com.benkih.estore.ledger.enums.LedgerTransactionType;
import com.benkih.estore.ledger.repository.LedgerAccountRepository;
import com.benkih.estore.ledger.repository.LedgerTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LedgerService implements ILedgerService{
  private final LedgerTransactionRepository transactionRepository;
  private final LedgerAccountRepository accountRepository;

  public LedgerTransaction post(
      LedgerTransactionType type,
      CurrencyCode currency,
      String reference,
      String description,
      List<LedgerPosting> postings
  ) {

    if (postings == null || postings.size() < 2) {
      throw new IllegalArgumentException("A ledger transaction must contain at least two postings");
    }

    BigDecimal totalDebit = postings.stream()
        .filter(p ->
            p.direction() == LedgerEntryDirection.DEBIT
        )
        .map(LedgerPosting::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalCredit = postings.stream()
        .filter(p ->
            p.direction() == LedgerEntryDirection.CREDIT
        )
        .map(LedgerPosting::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (totalDebit.compareTo(totalCredit) != 0) {
      throw new IllegalStateException(
          "Unbalanced ledger transaction. Debit="
              + totalDebit
              + ", Credit="
              + totalCredit
      );
    }

    if (transactionRepository.existsByReference(reference)) {
      throw new IllegalStateException(
          "Ledger transaction already exists: "
              + reference
      );
    }

    LedgerTransaction transaction = new LedgerTransaction();

    transaction.setType(type);
    transaction.setCurrency(currency);
    transaction.setReference(reference);
    transaction.setDescription(description);
    transaction.setTotalDebit(totalDebit);
    transaction.setTotalCredit(totalCredit);

    for (LedgerPosting posting : postings) {

      LedgerEntry entry = new LedgerEntry();

      entry.setTransaction(transaction);
      entry.setAccount(posting.account());
      entry.setBusiness(
          posting.account().getBusiness()
      );
//      entry.setType(posting.entryType());
      entry.setDirection(posting.direction());
      entry.setAmount(posting.amount());
      entry.setCurrency(currency);
      entry.setAllocation(posting.allocation());
      entry.setSettlement(posting.settlement());
      entry.setPayout(posting.payout());
      entry.setDescription(posting.description());

      transaction.getEntries().add(entry);
    }

    return transactionRepository.save(transaction);
  }
}
