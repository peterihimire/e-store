package com.benkih.estore.payout.service;

import com.benkih.estore.business.entity.BankAccount;
import com.benkih.estore.business.entity.Business;
import com.benkih.estore.business.entity.BusinessBalance;
import com.benkih.estore.business.service.BusinessBalanceService;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.ledger.dto.LedgerPosting;
import com.benkih.estore.ledger.entity.LedgerAccount;
import com.benkih.estore.ledger.enums.LedgerAccountType;
import com.benkih.estore.ledger.enums.LedgerEntryDirection;
import com.benkih.estore.ledger.enums.LedgerEntryType;
import com.benkih.estore.ledger.enums.LedgerTransactionType;
import com.benkih.estore.ledger.service.LedgerAccountService;
import com.benkih.estore.ledger.service.LedgerService;
import com.benkih.estore.payout.entity.Payout;
import com.benkih.estore.payout.enums.PayoutStatus;
import com.benkih.estore.payout.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PayoutService {

  private final PayoutRepository payoutRepository;
  private final LedgerService ledgerService;
  private final LedgerAccountService ledgerAccountService;
  private final BusinessBalanceService balanceService;

  public Payout requestPayout(
      Business business,
      BankAccount bankAccount,
      CurrencyCode currency,
      BigDecimal amount,
      String idempotencyKey
  ) {

    if (amount.signum() <= 0) {
      throw new BadRequestException("Payout amount must be greater than zero");
    }

    if (
        payoutRepository.existsByIdempotencyKey(
            idempotencyKey
        )
    ) {
      throw new AlreadyExistsException("Payout request already exists");
    }

    BusinessBalance balance = balanceService.getOrCreate(
            business,
            currency
        );

    if (
        balance.getAvailableBalance()
            .compareTo(amount) < 0
    ) {
      throw new BadRequestException("Insufficient available balance");
    }

    Payout payout = new Payout();

    payout.setBusiness(business);
    payout.setBankAccount(bankAccount);
    payout.setCurrency(currency);
    payout.setNetAmount(amount);
    payout.setStatus(
        PayoutStatus.REQUESTED
    );
    payout.setIdempotencyKey(
        idempotencyKey
    );
    payout.setRequestedAt(
        Instant.now()
    );

    payout = payoutRepository.save(payout);

    LedgerAccount available = ledgerAccountService.getOrCreateSellerAccount(
            business,
            LedgerAccountType.SELLER_AVAILABLE,
            currency
        );

    LedgerAccount payoutProcessing = ledgerAccountService.getOrCreatePlatformAccount(
            LedgerAccountType.PAYOUT_PROCESSING,
            currency
        );

    ledgerService.post(
        LedgerTransactionType.PAYOUT_INITIATED,
        currency,
        "PAYOUT-" + payout.getSlug(),
        "Payout initiated",
        List.of(

            new LedgerPosting(
                available,
                LedgerEntryType.SELLER_AVAILABLE,
                LedgerEntryDirection.DEBIT,
                amount,
                null,
                null,
                payout,
                "Seller payout"
            ),

            new LedgerPosting(
                payoutProcessing,
                LedgerEntryType.PAYOUT_PROCESSING,
                LedgerEntryDirection.CREDIT,
                amount,
                null,
                null,
                payout,
                "Payout processing liability"
            )
        )
    );

    balanceService.moveAvailableToPayout(
        business,
        currency,
        amount
    );

    payout.setStatus(PayoutStatus.PROCESSING);

    return payoutRepository.save(payout);
  }


  @Transactional
  public void markPayoutSuccessful(
      Payout payout,
      String providerReference
  ) {

    if (
        payout.getStatus()
            != PayoutStatus.PROCESSING
    ) {
      throw new IllegalStateException(
          "Payout is not processing"
      );
    }

    CurrencyCode currency =
        payout.getCurrency();

    BigDecimal amount =
        payout.getNetAmount();

    LedgerAccount payoutProcessing =
        ledgerAccountService.getOrCreatePlatformAccount(
            LedgerAccountType.PAYOUT_PROCESSING,
            currency
        );

    /*
     * In the complete ledger you would have a
     * platform cash/bank account as the other side.
     */
    LedgerAccount bank =
        ledgerAccountService.getOrCreatePlatformAccount(
            LedgerAccountType.PLATFORM_REVENUE,
            currency
        );

    ledgerService.post(
        LedgerTransactionType.PAYOUT_COMPLETED,
        currency,
        "PAYOUT-COMPLETE-" + payout.getSlug(),
        "Payout completed",
        List.of(

            new LedgerPosting(
                payoutProcessing,
                LedgerEntryType.PAYOUT_PROCESSING,
                LedgerEntryDirection.DEBIT,
                amount,
                null,
                null,
                payout,
                "Clear payout processing"
            ),

            new LedgerPosting(
                bank,
                LedgerEntryType.PLATFORM_REVENUE,
                LedgerEntryDirection.CREDIT,
                amount,
                null,
                null,
                payout,
                "Funds paid to seller"
            )
        )
    );

    payout.setStatus(
        PayoutStatus.SUCCESS
    );

    payout.setProviderReference(
        providerReference
    );

    payout.setProcessedAt(
        Instant.now()
    );

    payoutRepository.save(payout);
  }
}
