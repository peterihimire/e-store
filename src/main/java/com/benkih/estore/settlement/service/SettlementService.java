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

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementService {

  private final SettlementRepository settlementRepository;
  private final SettlementItemRepository settlementItemRepository;
  private final AllocationRepository allocationRepository;
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

    BigDecimal total = BigDecimal.ZERO;

    Settlement settlement = new Settlement();

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
      BigDecimal amount = allocation.getNetAmount();

      SettlementItem item = new SettlementItem();

      item.setSettlement(settlement);
      item.setAllocation(allocation);
      item.setBusiness(business);
      item.setAmount(amount);
      item.setCurrency(currency);

      settlement.getItems().add(item);

      total = total.add(amount);
    }

    settlement.setNetAmount(total);
    settlement.setEligibleAt(Instant.now());

    return settlementRepository.save(settlement);
  }

  public void releaseSettlement(
      Settlement settlement
  ) {

    if (
        settlement.getStatus()
            != SettlementStatus.PROCESSING
    ) {
      throw new IllegalStateException(
          "Settlement is not processing"
      );
    }

    Business business =
        settlement.getBusiness();

    CurrencyCode currency =
        settlement.getCurrency();

    BigDecimal amount =
        settlement.getNetAmount();

    LedgerAccount pending = ledgerAccountService.getOrCreateSellerAccount(
            business,
            LedgerAccountType.SELLER_PENDING,
            currency
        );

    LedgerAccount available =
        ledgerAccountService.getOrCreateSellerAccount(
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
                LedgerEntryType.SELLER_PENDING,
                LedgerEntryDirection.DEBIT,
                amount,
                null,
                settlement,
                null,
                "Move seller funds from pending"
            ),

            new LedgerPosting(
                available,
                LedgerEntryType.SELLER_AVAILABLE,
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

    settlement.setStatus(
        SettlementStatus.SETTLED
    );

    settlement.setSettledAt(
        Instant.now()
    );

    settlementRepository.save(settlement);
  }


}
