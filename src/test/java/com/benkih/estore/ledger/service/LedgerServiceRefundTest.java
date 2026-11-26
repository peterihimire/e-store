package com.benkih.estore.ledger.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.enums.AllocationStatus;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.ledger.entity.LedgerAccount;
import com.benkih.estore.ledger.entity.LedgerTransaction;
import com.benkih.estore.ledger.enums.LedgerAccountType;
import com.benkih.estore.ledger.enums.LedgerEntryDirection;
import com.benkih.estore.ledger.repository.LedgerTransactionRepository;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.refund.entity.Refund;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LedgerServiceRefundTest {

    @Mock private LedgerTransactionRepository transactionRepository;

    @Mock private LedgerAccountService ledgerAccountService;

    private LedgerRefundService ledgerRefundService;

    @BeforeEach
    void setUp() {
        ledgerRefundService =
                new LedgerRefundService(
                        transactionRepository,
                        ledgerAccountService,
                        new LedgerPostingService(transactionRepository));
    }

    @Test
    void postsBalancedSellerReversal() {
        Business business = org.mockito.Mockito.mock(Business.class);
        Allocation allocation = new Allocation();
        allocation.setBusiness(business);
        allocation.setStatus(AllocationStatus.PENDING);

        LedgerAccount sellerAccount =
                new LedgerAccount(business, LedgerAccountType.SELLER_PENDING, CurrencyCode.NGN);
        LedgerAccount refundPayable =
                new LedgerAccount(null, LedgerAccountType.REFUND_PAYABLE, CurrencyCode.NGN);

        when(transactionRepository.existsByReference("REFUND-REF-123")).thenReturn(false);
        when(ledgerAccountService.getOrCreateSellerAccount(
                        business, LedgerAccountType.SELLER_PENDING, CurrencyCode.NGN))
                .thenReturn(sellerAccount);
        when(ledgerAccountService.getOrCreatePlatformAccount(
                        LedgerAccountType.REFUND_PAYABLE, CurrencyCode.NGN))
                .thenReturn(refundPayable);
        when(transactionRepository.save(any(LedgerTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean posted =
                ledgerRefundService.recordRefund(
                        refund(), Map.of(allocation, new BigDecimal("1000.00")));

        assertTrue(posted);

        ArgumentCaptor<LedgerTransaction> captor = ArgumentCaptor.forClass(LedgerTransaction.class);
        verify(transactionRepository).save(captor.capture());

        LedgerTransaction transaction = captor.getValue();
        assertEquals(new BigDecimal("1000.00"), transaction.getTotalDebit());
        assertEquals(new BigDecimal("1000.00"), transaction.getTotalCredit());
        assertEquals(2, transaction.getEntries().size());
        assertEquals(LedgerEntryDirection.DEBIT, transaction.getEntries().get(0).getDirection());
        assertEquals(LedgerEntryDirection.CREDIT, transaction.getEntries().get(1).getDirection());
    }

    @Test
    void skipsDuplicateRefundWebhook() {
        when(transactionRepository.existsByReference("REFUND-REF-123")).thenReturn(true);

        Allocation allocation = new Allocation();
        allocation.setStatus(AllocationStatus.PENDING);

        boolean posted =
                ledgerRefundService.recordRefund(
                        refund(), Map.of(allocation, new BigDecimal("1000.00")));

        assertFalse(posted);
        verify(transactionRepository, never()).save(any(LedgerTransaction.class));
    }

    private Refund refund() {
        Order order = new Order();
        order.setCurrency(CurrencyCode.NGN);

        Payment payment = new Payment();
        payment.setOrder(order);

        Refund refund = new Refund();
        refund.setReference("REF-123");
        refund.setPayment(payment);

        return refund;
    }
}
