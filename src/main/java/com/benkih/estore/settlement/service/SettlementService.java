package com.benkih.estore.settlement.service;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.allocation.repository.AllocationRepository;
import com.benkih.estore.business.entity.Business;
import com.benkih.estore.business.service.BusinessBalanceService;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.ledger.dto.LedgerPosting;
import com.benkih.estore.ledger.entity.LedgerAccount;
import com.benkih.estore.ledger.enums.LedgerAccountType;
import com.benkih.estore.ledger.enums.LedgerEntryDirection;
import com.benkih.estore.ledger.enums.LedgerEntryType;
import com.benkih.estore.ledger.enums.LedgerTransactionType;
import com.benkih.estore.ledger.service.LedgerAccountService;
import com.benkih.estore.ledger.service.LedgerService;
import com.benkih.estore.settlement.entity.Settlement;
import com.benkih.estore.settlement.entity.SettlementItem;
import com.benkih.estore.settlement.enums.SettlementStatus;
import com.benkih.estore.settlement.repository.SettlementItemRepository;
import com.benkih.estore.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementService {

  private final SettlementRepository settlementRepository;
  private final LedgerService ledgerService;
  private final LedgerAccountService ledgerAccountService;
  private final BusinessBalanceService balanceService;

  public Settlement createSettlement(
      Business business,
      CurrencyCode currency,
      List<Allocation> allocations,
      Instant periodStart,
      Instant periodEnd
  ) {

    if (allocations.isEmpty()) {
      throw new BadRequestException("No allocations available for settlement");
    }

//    BigDecimal total = BigDecimal.ZERO;


    BigDecimal grossAmount = BigDecimal.ZERO;
    BigDecimal discountAmount = BigDecimal.ZERO;
    BigDecimal platformFee = BigDecimal.ZERO;
    BigDecimal paymentFee = BigDecimal.ZERO;
    BigDecimal taxAmount = BigDecimal.ZERO;
    BigDecimal shippingAmount = BigDecimal.ZERO;
    BigDecimal refundAmount = BigDecimal.ZERO;
    BigDecimal netAmount = BigDecimal.ZERO;

    Settlement settlement = new Settlement();

    settlement.setSettlementNumber(generateSettlementNumber());
    settlement.setBusiness(business);
    settlement.setCurrency(currency);
    settlement.setPeriodStart(periodStart);
    settlement.setPeriodEnd(periodEnd);
    settlement.setStatus(SettlementStatus.PROCESSING);

    for (Allocation allocation : allocations) {

      if (
          !allocation.getBusiness()
              .getId()
              .equals(business.getId())
      ) {
        throw new IllegalStateException("Allocation belongs to another business");
      }

      if (
          allocation.getCurrency() != currency
      ) {
        throw new IllegalStateException("Settlement currency mismatch");
      }

      /*
       * This amount comes from Allocation.
       * Settlement does not recalculate the commercial split.
       */
//      BigDecimal amount = allocation.getNetAmount();
      grossAmount = grossAmount.add(
          allocation.getGrossAmount()
      );

      discountAmount = discountAmount.add(
          allocation.getDiscountAmount()
      );

      platformFee = platformFee.add(
          allocation.getPlatformFee()
      );

      paymentFee = paymentFee.add(
          allocation.getPaymentFee()
      );

      taxAmount = taxAmount.add(
          allocation.getTaxAmount()
      );

      shippingAmount = shippingAmount.add(
          allocation.getShippingAmount()
      );

      refundAmount = refundAmount.add(
          allocation.getRefundAmount()
      );

      netAmount = netAmount.add(
          allocation.getNetAmount()
      );

      SettlementItem item = new SettlementItem();

      item.setSettlement(settlement);
      item.setAllocation(allocation);
      item.setBusiness(business);
//      item.setAmount(amount);
      item.setAmount(allocation.getNetAmount());
      item.setCurrency(currency);

      settlement.getItems().add(item);

//      total = total.add(amount);
    }

    settlement.setGrossAmount(grossAmount);
    settlement.setDiscountAmount(discountAmount);
    settlement.setPlatformFee(platformFee);
    settlement.setPaymentFee(paymentFee);
    settlement.setTaxAmount(taxAmount);
    settlement.setShippingAmount(shippingAmount);
    settlement.setRefundAmount(refundAmount);
    settlement.setAdjustmentAmount(BigDecimal.ZERO);

//    settlement.setNetAmount(total);
    settlement.setNetAmount(netAmount);
    settlement.setEligibleAt(Instant.now());

    return settlementRepository.save(settlement);
  }


  public void releaseSettlement(Settlement settlement) {

    if (settlement.getStatus() != SettlementStatus.PROCESSING) {
      throw new IllegalStateException("Settlement is not processing");
    }

    Business business = settlement.getBusiness();

    CurrencyCode currency = settlement.getCurrency();

    BigDecimal amount = settlement.getNetAmount();

    LedgerAccount pending = ledgerAccountService.getOrCreateSellerAccount(
            business,
            LedgerAccountType.SELLER_PENDING,
            currency
        );

    LedgerAccount available = ledgerAccountService.getOrCreateSellerAccount(
            business,
            LedgerAccountType.SELLER_AVAILABLE,
            currency
        );

    ledgerService.post(
        LedgerTransactionType.SETTLEMENT_RELEASE,
        currency,
        "SETTLEMENT-" + settlement.getSlug(),
        "Release settlement " + settlement.getSlug(),
        List.of(

            new LedgerPosting(
                pending,
//                LedgerEntryType.SELLER_PENDING,
                LedgerEntryDirection.DEBIT,
                amount,
                null,
                settlement,
                null,
                "Move seller funds from pending"
            ),

            new LedgerPosting(
                available,
//                LedgerEntryType.SELLER_AVAILABLE,
                LedgerEntryDirection.CREDIT,
                amount,
                null,
                settlement,
                null,
                "Move seller funds to available"
            )
        )
    );

    balanceService.movePendingToAvailable(
        business,
        currency,
        amount
    );

    settlement.setStatus(SettlementStatus.SETTLED);

    settlement.setSettledAt(Instant.now());

    settlementRepository.save(settlement);
  }

  private String generateSettlementNumber() {
    return "SET-" + UUID.randomUUID()
        .toString()
        .replace("-", "")
        .substring(0, 12)
        .toUpperCase();
  }
}
// processing order, marks orders , creates allocation, ledgers and business
// balance

// cron that runs to check if product sold to a customer has reach return window

// create settlement for the business and once all requirements has been met,
// then settlement is moved from pending to available

// the seller can withdraw , which is a seller can create payout