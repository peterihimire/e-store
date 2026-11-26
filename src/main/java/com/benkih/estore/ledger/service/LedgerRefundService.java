package com.benkih.estore.ledger.service;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.common.enums.AllocationStatus;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.ledger.dto.LedgerPosting;
import com.benkih.estore.ledger.entity.LedgerAccount;
import com.benkih.estore.ledger.enums.LedgerAccountType;
import com.benkih.estore.ledger.enums.LedgerEntryDirection;
import com.benkih.estore.ledger.enums.LedgerTransactionType;
import com.benkih.estore.ledger.repository.LedgerTransactionRepository;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.refund.entity.Refund;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LedgerRefundService {
    private final LedgerTransactionRepository transactionRepository;
    private final LedgerAccountService ledgerAccountService;
    private final LedgerPostingService ledgerPostingService;

    public boolean recordRefund(Refund refund, Map<Allocation, BigDecimal> reversals) {
        if (refund == null) {
            throw new IllegalArgumentException("Refund is required");
        }

        if (reversals == null || reversals.isEmpty()) {
            return false;
        }

        Payment payment = refund.getPayment();

        if (payment == null || payment.getOrder() == null) {
            throw new IllegalArgumentException(
                    "Refund must be associated with a payment and order");
        }

        CurrencyCode currency = payment.getOrder().getCurrency();
        String reference = "REFUND-" + refund.getReference();

        /*
         * Idempotency: gateway webhooks can be delivered more than once.
         */
        if (transactionRepository.existsByReference(reference)) {
            log.info(
                    "Refund ledger transaction already exists for {}. Skipping.",
                    refund.getReference());
            return false;
        }

        List<LedgerPosting> postings = new ArrayList<>();
        BigDecimal totalReversal = BigDecimal.ZERO;

        for (Map.Entry<Allocation, BigDecimal> reversal : reversals.entrySet()) {
            Allocation allocation = reversal.getKey();
            BigDecimal amount = reversal.getValue();

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            AllocationStatus status = allocation.getStatus();

            boolean released =
                    status == AllocationStatus.SETTLED
                            || status == AllocationStatus.AVAILABLE
                            || status == AllocationStatus.PAID;

            LedgerAccount sellerAccount =
                    ledgerAccountService.getOrCreateSellerAccount(
                            allocation.getBusiness(),
                            released
                                    ? LedgerAccountType.SELLER_AVAILABLE
                                    : LedgerAccountType.SELLER_PENDING,
                            currency);

            postings.add(
                    new LedgerPosting(
                            sellerAccount,
                            LedgerEntryDirection.DEBIT,
                            amount,
                            allocation,
                            null,
                            null,
                            "Reverse seller entitlement for refund " + refund.getReference()));

            totalReversal = totalReversal.add(amount);
        }

        if (totalReversal.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        LedgerAccount refundPayable =
                ledgerAccountService.getOrCreatePlatformAccount(
                        LedgerAccountType.REFUND_PAYABLE, currency);

        postings.add(
                new LedgerPosting(
                        refundPayable,
                        LedgerEntryDirection.CREDIT,
                        totalReversal,
                        null,
                        null,
                        null,
                        "Amount owed to customer for refund " + refund.getReference()));

        ledgerPostingService.post(
                LedgerTransactionType.REFUND,
                currency,
                reference,
                "Refund " + refund.getReference(),
                postings);

        return true;
    }
}
