package com.benkih.estore.ledger.service;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.ledger.dto.LedgerPosting;
import com.benkih.estore.ledger.entity.LedgerTransaction;
import com.benkih.estore.ledger.enums.LedgerTransactionType;
import com.benkih.estore.payment.entity.Payment;

import java.util.List;

public interface ILedgerService {
  LedgerTransaction post(LedgerTransactionType type,
                         CurrencyCode currency,
                         String reference,
                         String description,
                         List<LedgerPosting> postings);

  void recordAllocations(List<Allocation> allocations);

  void recordPlatformFees(List<Allocation> allocations);

  void recordPaymentReceived(Payment payment);

  void recordProcessorFee(Payment payment);

  void recordTax(List<Allocation> allocations);

  void recordShipping(List<Allocation> allocations);
}
