package com.benkih.estore.ledger.service;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.ledger.dto.LedgerPosting;
import com.benkih.estore.ledger.entity.LedgerAccount;
import com.benkih.estore.ledger.enums.LedgerAccountType;
import com.benkih.estore.ledger.enums.LedgerEntryDirection;
import com.benkih.estore.ledger.enums.LedgerTransactionType;
import com.benkih.estore.ledger.repository.LedgerTransactionRepository;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.payment.entity.Payment;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LedgerAllocationService {
    private final LedgerTransactionRepository transactionRepository;
    private final LedgerAccountService ledgerAccountService;
    private final LedgerPostingService ledgerPostingService;

    @Transactional
    public void recordAllocations(List<Allocation> allocations) {

        if (allocations == null || allocations.isEmpty()) {
            return;
        }

        Allocation firstAllocation = allocations.get(0);

        Payment payment = firstAllocation.getPayment();

        if (payment == null) {
            throw new IllegalArgumentException("Allocation must be associated with a payment");
        }

        Order order = payment.getOrder();

        if (order == null) {
            throw new IllegalArgumentException("Payment must be associated with an order");
        }

        CurrencyCode currency = order.getCurrency();

        String reference = "ALLOCATION-" + payment.getReference();

        /*
         * Idempotency:
         * A webhook can be delivered more than once.
         */
        if (transactionRepository.existsByReference(reference)) {
            log.info(
                    "Allocation ledger transaction already exists for payment {}",
                    payment.getReference());
            return;
        }

        /*
         * Total amount that will become seller pending earnings.
         */
        BigDecimal totalSellerAmount =
                allocations.stream()
                        .map(Allocation::getNetAmount)
                        .filter(Objects::nonNull)
                        .filter(amount -> amount.compareTo(BigDecimal.ZERO) > 0)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSellerAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        /*
         * The payment liability created when the customer payment
         * was received.
         */
        LedgerAccount customerPaymentLiability =
                ledgerAccountService.getOrCreatePlatformAccount(
                        LedgerAccountType.CUSTOMER_PAYMENT_LIABILITY, currency);

        List<LedgerPosting> postings = new ArrayList<>();

        /*
         * Debit the customer payment liability for the amount
         * being transferred into seller pending balances.
         */
        postings.add(
                new LedgerPosting(
                        customerPaymentLiability,
                        LedgerEntryDirection.DEBIT,
                        totalSellerAmount,
                        null,
                        null,
                        null,
                        "Allocate payment to sellers"));

        /*
         * Credit each seller's pending account.
         */
        for (Allocation allocation : allocations) {

            BigDecimal amount = allocation.getNetAmount();

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            LedgerAccount sellerPending =
                    ledgerAccountService.getOrCreateSellerAccount(
                            allocation.getBusiness(), LedgerAccountType.SELLER_PENDING, currency);

            postings.add(
                    new LedgerPosting(
                            sellerPending,
                            LedgerEntryDirection.CREDIT,
                            amount,
                            allocation,
                            null,
                            null,
                            "Seller pending earnings"));
        }

        ledgerPostingService.post(
                LedgerTransactionType.ALLOCATION_POSTED,
                currency,
                reference,
                "Seller allocations for payment " + payment.getReference(),
                postings);
    }

    @Transactional
    public void recordPlatformFees(List<Allocation> allocations) {

        if (allocations == null || allocations.isEmpty()) {
            return;
        }

        Payment payment = allocations.get(0).getPayment();

        if (payment == null || payment.getOrder() == null) {
            throw new IllegalArgumentException(
                    "Allocation must be associated with a payment and order");
        }

        CurrencyCode currency = payment.getOrder().getCurrency();

        String reference = "PLATFORM-FEE-" + payment.getReference();

        if (transactionRepository.existsByReference(reference)) {
            log.info(
                    "Platform fee ledger transaction already exists for payment {}",
                    payment.getReference());
            return;
        }

        BigDecimal totalPlatformFee =
                allocations.stream()
                        .map(Allocation::getPlatformFee)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPlatformFee.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        LedgerAccount paymentLiability =
                ledgerAccountService.getOrCreatePlatformAccount(
                        LedgerAccountType.CUSTOMER_PAYMENT_LIABILITY, currency);

        LedgerAccount platformRevenue =
                ledgerAccountService.getOrCreatePlatformAccount(
                        LedgerAccountType.PLATFORM_REVENUE, currency);

        ledgerPostingService.post(
                LedgerTransactionType.PLATFORM_FEE,
                currency,
                reference,
                "Platform fees for payment " + payment.getReference(),
                List.of(
                        new LedgerPosting(
                                paymentLiability,
                                LedgerEntryDirection.DEBIT,
                                totalPlatformFee,
                                null,
                                null,
                                null,
                                "Platform commission"),
                        new LedgerPosting(
                                platformRevenue,
                                LedgerEntryDirection.CREDIT,
                                totalPlatformFee,
                                null,
                                null,
                                null,
                                "Benkih platform commission")));
    }

    @Transactional
    public void recordTax(List<Allocation> allocations) {

        if (allocations == null || allocations.isEmpty()) {
            return;
        }

        Payment payment = allocations.get(0).getPayment();

        if (payment == null || payment.getOrder() == null) {
            throw new IllegalArgumentException(
                    "Allocation must be associated with a payment and order");
        }

        CurrencyCode currency = payment.getOrder().getCurrency();

        String reference = "TAX-COLLECTED-" + payment.getReference();

        if (transactionRepository.existsByReference(reference)) {
            log.info(
                    "Tax ledger transaction already exists for payment {}", payment.getReference());
            return;
        }

        BigDecimal totalTax =
                allocations.stream()
                        .map(Allocation::getTaxAmount)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalTax.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        LedgerAccount customerPaymentLiability =
                ledgerAccountService.getOrCreatePlatformAccount(
                        LedgerAccountType.CUSTOMER_PAYMENT_LIABILITY, currency);

        LedgerAccount taxPayable =
                ledgerAccountService.getOrCreatePlatformAccount(
                        LedgerAccountType.TAX_PAYABLE, currency);

        ledgerPostingService.post(
                LedgerTransactionType.TAX_COLLECTED, // or create TAX_COLLECTED
                currency,
                reference,
                "Tax collected for payment " + payment.getReference(),
                List.of(
                        new LedgerPosting(
                                customerPaymentLiability,
                                LedgerEntryDirection.DEBIT,
                                totalTax,
                                null,
                                null,
                                null,
                                "Customer tax liability"),
                        new LedgerPosting(
                                taxPayable,
                                LedgerEntryDirection.CREDIT,
                                totalTax,
                                null,
                                null,
                                null,
                                "Tax payable")));
    }

    @Transactional
    public void recordShipping(List<Allocation> allocations) {

        if (allocations == null || allocations.isEmpty()) {
            return;
        }

        Payment payment = allocations.get(0).getPayment();

        if (payment == null || payment.getOrder() == null) {
            throw new IllegalArgumentException(
                    "Allocation must be associated with a payment and order");
        }

        CurrencyCode currency = payment.getOrder().getCurrency();

        String reference = "SHIPPING-" + payment.getReference();

        if (transactionRepository.existsByReference(reference)) {
            log.info(
                    "Shipping ledger transaction already exists for payment {}",
                    payment.getReference());
            return;
        }

        BigDecimal totalShipping =
                allocations.stream()
                        .map(Allocation::getShippingAmount)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalShipping.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        LedgerAccount customerPaymentLiability =
                ledgerAccountService.getOrCreatePlatformAccount(
                        LedgerAccountType.CUSTOMER_PAYMENT_LIABILITY, currency);

        LedgerAccount shippingRevenue =
                ledgerAccountService.getOrCreatePlatformAccount(
                        LedgerAccountType.LOGISTICS_PAYABLE, currency);

        ledgerPostingService.post(
                LedgerTransactionType.SHIPPING_CHARGE,
                currency,
                reference,
                "Shipping collected for payment " + payment.getReference(),
                List.of(
                        new LedgerPosting(
                                customerPaymentLiability,
                                LedgerEntryDirection.DEBIT,
                                totalShipping,
                                null,
                                null,
                                null,
                                "Customer shipping payment"),
                        new LedgerPosting(
                                shippingRevenue,
                                LedgerEntryDirection.CREDIT,
                                totalShipping,
                                null,
                                null,
                                null,
                                "Amount payable to logistics provider")));
    }
}
