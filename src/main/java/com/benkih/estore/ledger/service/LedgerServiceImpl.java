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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LedgerServiceImpl implements LedgerService {
    private final LedgerPostingService ledgerPostingService;
    private final LedgerPaymentService ledgerPaymentService;
    private final LedgerAllocationService ledgerAllocationService;
    private final LedgerRefundService ledgerRefundService;

    @Override
    public LedgerTransaction post(
            LedgerTransactionType type,
            CurrencyCode currency,
            String reference,
            String description,
            List<LedgerPosting> postings) {

        return ledgerPostingService.post(type, currency, reference, description, postings);
    }

    @Transactional
    @Override
    public void recordPaymentReceived(Payment payment) {
        ledgerPaymentService.recordPaymentReceived(payment);
    }

    @Transactional
    @Override
    public void recordAllocations(List<Allocation> allocations) {
        ledgerAllocationService.recordAllocations(allocations);
    }

    @Transactional
    @Override
    public void recordPlatformFees(List<Allocation> allocations) {
        ledgerAllocationService.recordPlatformFees(allocations);
    }

    @Transactional
    @Override
    public void recordProcessorFee(Payment payment) {
        ledgerPaymentService.recordProcessorFee(payment);
    }

    @Transactional
    @Override
    public void recordTax(List<Allocation> allocations) {
        ledgerAllocationService.recordTax(allocations);
    }

    @Transactional
    @Override
    public void recordShipping(List<Allocation> allocations) {
        ledgerAllocationService.recordShipping(allocations);
    }

    @Transactional
    @Override
    public void recordProcessorFeeRecovery(Payment payment) {
        ledgerPaymentService.recordProcessorFeeRecovery(payment);
    }

    @Override
    public boolean recordRefund(Refund refund, Map<Allocation, BigDecimal> reversals) {
        return ledgerRefundService.recordRefund(refund, reversals);
    }
}
// when logistic company is paid
// DR  LOGISTICS_PAYABLE             ₦3,500
// CR  PLATFORM_CASH                 ₦3,500
