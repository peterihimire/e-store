package com.benkih.estore.ledger.service;

import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.ledger.dto.LedgerPosting;
import com.benkih.estore.ledger.entity.LedgerEntry;
import com.benkih.estore.ledger.entity.LedgerTransaction;
import com.benkih.estore.ledger.enums.LedgerEntryDirection;
import com.benkih.estore.ledger.enums.LedgerTransactionType;
import com.benkih.estore.ledger.repository.LedgerTransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LedgerPostingService {
    private final LedgerTransactionRepository transactionRepository;

    public LedgerTransaction post(
            LedgerTransactionType type,
            CurrencyCode currency,
            String reference,
            String description,
            List<LedgerPosting> postings) {

        // BASIC VALIDATION
        if (type == null) {
            throw new IllegalArgumentException("Ledger transaction type is required");
        }

        if (currency == null) {
            throw new IllegalArgumentException("Currency is required");
        }

        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("Ledger transaction reference is required");
        }

        if (postings == null || postings.size() < 2) {
            throw new IllegalArgumentException(
                    "A ledger transaction must contain at least two postings");
        }

        // VALIDATE POSTINGS
        for (LedgerPosting posting : postings) {

            if (posting == null) {
                throw new IllegalArgumentException("Ledger posting cannot be null");
            }

            if (posting.account() == null) {
                throw new IllegalArgumentException("Ledger posting account is required");
            }

            if (posting.direction() == null) {
                throw new IllegalArgumentException("Ledger posting direction is required");
            }

            if (posting.amount() == null || posting.amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Ledger posting amount must be greater than zero");
            }

            if (posting.account().getCurrency() != currency) {
                throw new IllegalStateException(
                        "Ledger account currency does not match transaction currency");
            }
        }

        // CALCULATE TOTALS
        BigDecimal totalDebit =
                postings.stream()
                        .filter(p -> p.direction() == LedgerEntryDirection.DEBIT)
                        .map(LedgerPosting::amount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit =
                postings.stream()
                        .filter(p -> p.direction() == LedgerEntryDirection.CREDIT)
                        .map(LedgerPosting::amount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        // BALANCE CHECK
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalStateException(
                    "Unbalanced ledger transaction. Debit="
                            + totalDebit
                            + ", Credit="
                            + totalCredit);
        }

        // IDEMPOTENCY CHECK
        if (transactionRepository.existsByReference(reference)) {
            throw new IllegalStateException("Ledger transaction already exists: " + reference);
        }

        // CREATE TRANSACTION
        LedgerTransaction transaction = new LedgerTransaction();

        transaction.setType(type);
        transaction.setCurrency(currency);
        transaction.setReference(reference);
        transaction.setDescription(description);
        transaction.setTotalDebit(totalDebit);
        transaction.setTotalCredit(totalCredit);

        // CREATE ENTRIES
        for (LedgerPosting posting : postings) {

            LedgerEntry entry = new LedgerEntry();

            entry.setTransaction(transaction);
            entry.setAccount(posting.account());
            entry.setBusiness(posting.account().getBusiness());
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
