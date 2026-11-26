package com.benkih.estore.ledger.service;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.ledger.dto.LedgerPosting;
import com.benkih.estore.ledger.entity.LedgerTransaction;
import com.benkih.estore.ledger.enums.LedgerTransactionType;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.refund.entity.Refund;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface LedgerService {
    LedgerTransaction post(
            LedgerTransactionType type,
            CurrencyCode currency,
            String reference,
            String description,
            List<LedgerPosting> postings);

    void recordPaymentReceived(Payment payment);

    void recordAllocations(List<Allocation> allocations);

    void recordPlatformFees(List<Allocation> allocations);

    void recordProcessorFee(Payment payment);

    void recordTax(List<Allocation> allocations);

    void recordShipping(List<Allocation> allocations);

    void recordProcessorFeeRecovery(Payment payment);

    boolean recordRefund(Refund refund, Map<Allocation, BigDecimal> reversals);
}
