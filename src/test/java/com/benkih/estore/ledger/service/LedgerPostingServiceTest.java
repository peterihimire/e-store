package com.benkih.estore.ledger.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.ledger.dto.LedgerPosting;
import com.benkih.estore.ledger.entity.LedgerAccount;
import com.benkih.estore.ledger.entity.LedgerTransaction;
import com.benkih.estore.ledger.enums.LedgerAccountType;
import com.benkih.estore.ledger.enums.LedgerEntryDirection;
import com.benkih.estore.ledger.enums.LedgerTransactionType;
import com.benkih.estore.ledger.repository.LedgerTransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LedgerPostingServiceTest {

    @Mock private LedgerTransactionRepository transactionRepository;

    private LedgerPostingService service;

    @BeforeEach
    void setUp() {
        service = new LedgerPostingService(transactionRepository);
    }

    @Test
    void rejectsUnbalancedPostings() {
        assertThrows(
                IllegalStateException.class,
                () -> service.post(type(), currency(), "REF-1", "Unbalanced", postings("90.00")));

        verify(transactionRepository, never()).save(any(LedgerTransaction.class));
    }

    @Test
    void rejectsDuplicateTransactionReference() {
        when(transactionRepository.existsByReference("REF-1")).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> service.post(type(), currency(), "REF-1", "Duplicate", postings("100.00")));

        verify(transactionRepository, never()).save(any(LedgerTransaction.class));
    }

    @Test
    void rejectsNonPositivePostingAmount() {
        List<LedgerPosting> postings =
                List.of(
                        posting(LedgerEntryDirection.DEBIT, BigDecimal.ZERO),
                        posting(LedgerEntryDirection.CREDIT, BigDecimal.ZERO));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.post(type(), currency(), "REF-1", "Zero", postings));
    }

    @Test
    void rejectsPostingsWithDifferentCurrency() {
        LedgerPosting credit =
                new LedgerPosting(
                        new LedgerAccount(null, LedgerAccountType.REFUND_PAYABLE, CurrencyCode.USD),
                        LedgerEntryDirection.CREDIT,
                        bd("100.00"),
                        null,
                        null,
                        null,
                        "Credit");

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.post(
                                type(),
                                currency(),
                                "REF-1",
                                "Currency mismatch",
                                List.of(
                                        posting(LedgerEntryDirection.DEBIT, bd("100.00")),
                                        credit)));
    }

    @Test
    void persistsBalancedTransactionTotalsAndEntries() {
        when(transactionRepository.existsByReference("REF-1")).thenReturn(false);
        when(transactionRepository.save(any(LedgerTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.post(type(), currency(), "REF-1", "Balanced", postings("100.00"));

        ArgumentCaptor<LedgerTransaction> captor = ArgumentCaptor.forClass(LedgerTransaction.class);
        verify(transactionRepository).save(captor.capture());

        LedgerTransaction transaction = captor.getValue();
        assertEquals(bd("100.00"), transaction.getTotalDebit());
        assertEquals(bd("100.00"), transaction.getTotalCredit());
        assertEquals(2, transaction.getEntries().size());
        assertEquals(LedgerEntryDirection.DEBIT, transaction.getEntries().get(0).getDirection());
        assertEquals(LedgerEntryDirection.CREDIT, transaction.getEntries().get(1).getDirection());
    }

    private LedgerTransactionType type() {
        return LedgerTransactionType.REFUND;
    }

    private CurrencyCode currency() {
        return CurrencyCode.NGN;
    }

    private List<LedgerPosting> postings(String creditAmount) {
        return List.of(
                posting(LedgerEntryDirection.DEBIT, bd("100.00")),
                posting(LedgerEntryDirection.CREDIT, bd(creditAmount)));
    }

    private LedgerPosting posting(LedgerEntryDirection direction, BigDecimal amount) {
        return new LedgerPosting(
                new LedgerAccount(null, LedgerAccountType.REFUND_PAYABLE, currency()),
                direction,
                amount,
                null,
                null,
                null,
                direction.name());
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
