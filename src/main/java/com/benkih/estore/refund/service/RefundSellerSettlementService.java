package com.benkih.estore.refund.service;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.allocation.repository.AllocationRepository;
import com.benkih.estore.business.service.BusinessBalanceService;
import com.benkih.estore.common.enums.AllocationStatus;
import com.benkih.estore.ledger.service.LedgerService;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.refund.entity.Refund;
import com.benkih.estore.refund.entity.RefundItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundSellerSettlementService {
    private final AllocationRepository allocationRepository;
    private final LedgerService ledgerService;
    private final BusinessBalanceService balanceService;

    public void recordSellerRefund(Refund refund, Payment payment) {
        List<Allocation> allocations = allocationRepository.findByPaymentId(payment.getId());

        Map<Long, Allocation> allocationsByOrderItem = new HashMap<>();

        for (Allocation allocation : allocations) {
            allocationsByOrderItem.put(allocation.getOrderItem().getId(), allocation);
        }

        Map<Allocation, BigDecimal> reversals = new LinkedHashMap<>();

        for (RefundItem item : refund.getItems()) {
            Allocation allocation = allocationsByOrderItem.get(item.getOrderItem().getId());

            if (allocation == null) {
                log.warn(
                        "No allocation found for refunded order item {}",
                        item.getOrderItem().getSlug());
                continue;
            }

            BigDecimal remaining = allocation.getNetAmount().subtract(allocation.getRefundAmount());

            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            int orderedQuantity = item.getOrderItem().getQuantity();

            if (orderedQuantity <= 0) {
                continue;
            }

            BigDecimal reversal =
                    allocation
                            .getNetAmount()
                            .multiply(BigDecimal.valueOf(item.getQuantity()))
                            .divide(BigDecimal.valueOf(orderedQuantity), 2, RoundingMode.HALF_UP)
                            .min(remaining);

            if (reversal.compareTo(BigDecimal.ZERO) > 0) {
                reversals.merge(allocation, reversal, BigDecimal::add);
            }
        }

        if (reversals.isEmpty()) {
            return;
        }

        if (!ledgerService.recordRefund(refund, reversals)) {
            log.info(
                    "Refund {} already recorded in the ledger. Skipping reversal.",
                    refund.getReference());
            return;
        }

        for (Map.Entry<Allocation, BigDecimal> reversal : reversals.entrySet()) {
            Allocation allocation = reversal.getKey();
            BigDecimal amount = reversal.getValue();
            boolean released = isSellerFundsReleased(allocation);

            BigDecimal refundAmount = allocation.getRefundAmount().add(amount);

            allocation.setRefundAmount(refundAmount);
            allocation.setStatus(
                    refundAmount.compareTo(allocation.getNetAmount()) >= 0
                            ? AllocationStatus.REFUNDED
                            : AllocationStatus.PARTIALLY_REFUNDED);

            allocationRepository.save(allocation);

            if (released) {
                balanceService.decreaseAvailable(
                        allocation.getBusiness(), allocation.getCurrency(), amount);
            } else {
                balanceService.decreasePending(
                        allocation.getBusiness(), allocation.getCurrency(), amount);
            }
        }
    }

    private boolean isSellerFundsReleased(Allocation allocation) {
        AllocationStatus status = allocation.getStatus();

        return status == AllocationStatus.SETTLED
                || status == AllocationStatus.AVAILABLE
                || status == AllocationStatus.PAID;
    }
}
