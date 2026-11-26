package com.benkih.estore.ledger.service;

import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.ledger.dto.LedgerPosting;
import com.benkih.estore.ledger.entity.LedgerAccount;
import com.benkih.estore.ledger.enums.LedgerAccountType;
import com.benkih.estore.ledger.enums.LedgerEntryDirection;
import com.benkih.estore.ledger.enums.LedgerTransactionType;
import com.benkih.estore.ledger.repository.LedgerTransactionRepository;
import com.benkih.estore.payment.entity.Payment;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LedgerPaymentService {
    private final LedgerTransactionRepository transactionRepository;
    private final LedgerAccountService ledgerAccountService;
    private final LedgerPostingService ledgerPostingService;

    @Transactional
    public void recordPaymentReceived(Payment payment) {

        if (payment == null) {
            throw new IllegalArgumentException("Payment is required");
        }

        if (payment.getOrder() == null) {
            throw new IllegalArgumentException("Payment must be associated with an order");
        }

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Only successful payments can be recorded");
        }

        CurrencyCode currency = payment.getOrder().getCurrency();

        BigDecimal amount = payment.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        String reference = "PAYMENT-" + payment.getReference();

        /*
         * Idempotency:
         * Paystack can send the same webhook more than once.
         */
        if (transactionRepository.existsByReference(reference)) {
            log.info(
                    "Payment ledger transaction already exists for {}. Skipping.",
                    payment.getReference());

            return;
        }

        /*
         * Paystack is holding the money on behalf of Benkih.
         *
         * This is an asset because Benkih has a claim to the funds.
         */
        LedgerAccount paymentProcessorBalance =
                ledgerAccountService.getOrCreatePlatformAccount(
                        LedgerAccountType.PAYMENT_PROCESSOR_BALANCE, currency);

        /*
         * Until the payment has been allocated to sellers,
         * the money represents an obligation that Benkih
         * must distribute/account for.
         */
        LedgerAccount customerPaymentLiability =
                ledgerAccountService.getOrCreatePlatformAccount(
                        LedgerAccountType.CUSTOMER_PAYMENT_LIABILITY, currency);

        ledgerPostingService.post(
                LedgerTransactionType.PAYMENT_RECEIVED,
                currency,
                reference,
                "Payment received from customer - " + payment.getReference(),
                List.of(
                        new LedgerPosting(
                                paymentProcessorBalance,
                                LedgerEntryDirection.DEBIT,
                                amount,
                                null,
                                null,
                                null,
                                "Funds received by payment processor"),
                        new LedgerPosting(
                                customerPaymentLiability,
                                LedgerEntryDirection.CREDIT,
                                amount,
                                null,
                                null,
                                null,
                                "Customer payment awaiting allocation")));
    }

    @Transactional
    public void recordProcessorFee(Payment payment) {

        if (payment == null) {
            throw new IllegalArgumentException("Payment is required");
        }

        if (payment.getOrder() == null) {
            throw new IllegalArgumentException("Payment must be associated with an order");
        }

        BigDecimal processorFee = payment.getProcessorFee();

        if (processorFee == null || processorFee.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        CurrencyCode currency = payment.getOrder().getCurrency();

        String reference = "PROCESSOR-FEE-" + payment.getReference();

        if (transactionRepository.existsByReference(reference)) {
            log.info(
                    "Processor fee ledger transaction already exists for payment {}",
                    payment.getReference());
            return;
        }

        LedgerAccount paymentProcessorBalance =
                ledgerAccountService.getOrCreatePlatformAccount(
                        LedgerAccountType.PAYMENT_PROCESSOR_BALANCE, currency);

        LedgerAccount processorExpense =
                ledgerAccountService.getOrCreatePlatformAccount(
                        LedgerAccountType.PAYMENT_PROCESSING_EXPENSE, currency);

        ledgerPostingService.post(
                LedgerTransactionType.PROCESSOR_FEE,
                currency,
                reference,
                "Payment processor fee for " + payment.getReference(),
                List.of(
                        new LedgerPosting(
                                processorExpense,
                                LedgerEntryDirection.DEBIT,
                                processorFee,
                                null,
                                null,
                                null,
                                "Payment processor fee"),
                        new LedgerPosting(
                                paymentProcessorBalance,
                                LedgerEntryDirection.CREDIT,
                                processorFee,
                                null,
                                null,
                                null,
                                "Processor deducted payment fee")));
    }

    @Transactional
    public void recordProcessorFeeRecovery(Payment payment) {

        if (payment == null) {
            throw new IllegalArgumentException("Payment is required");
        }

        if (payment.getOrder() == null) {
            throw new IllegalArgumentException("Payment must be associated with an order");
        }

        BigDecimal processorFee = payment.getProcessorFee();

        if (processorFee == null || processorFee.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        CurrencyCode currency = payment.getOrder().getCurrency();

        String reference = "PROCESSOR-FEE-RECOVERY-" + payment.getReference();

        if (transactionRepository.existsByReference(reference)) {

            log.info(
                    "Processor fee recovery already exists for payment {}", payment.getReference());

            return;
        }

        LedgerAccount customerPaymentLiability =
                ledgerAccountService.getOrCreatePlatformAccount(
                        LedgerAccountType.CUSTOMER_PAYMENT_LIABILITY, currency);

        LedgerAccount processorFeeRecovery =
                ledgerAccountService.getOrCreatePlatformAccount(
                        LedgerAccountType.PROCESSOR_FEE_REIMBURSEMENT, currency);

        ledgerPostingService.post(
                LedgerTransactionType.PROCESSOR_FEE_RECOVERY,
                currency,
                reference,
                "Processor fee recovered from seller for payment " + payment.getReference(),
                List.of(
                        new LedgerPosting(
                                customerPaymentLiability,
                                LedgerEntryDirection.DEBIT,
                                processorFee,
                                null,
                                null,
                                null,
                                "Processor fee recovered from seller"),
                        new LedgerPosting(
                                processorFeeRecovery,
                                LedgerEntryDirection.CREDIT,
                                processorFee,
                                null,
                                null,
                                null,
                                "Seller processor fee recovery")));
    }
}
