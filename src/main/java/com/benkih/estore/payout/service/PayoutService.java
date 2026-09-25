package com.benkih.estore.payout.service;

import com.benkih.estore.business.entity.BankAccount;
import com.benkih.estore.business.entity.Business;
import com.benkih.estore.business.entity.BusinessBalance;
import com.benkih.estore.business.repository.BankAccountRepository;
import com.benkih.estore.business.repository.BusinessRepository;
import com.benkih.estore.business.service.BusinessBalanceService;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.ledger.dto.LedgerPosting;
import com.benkih.estore.ledger.entity.LedgerAccount;
import com.benkih.estore.ledger.enums.LedgerAccountType;
import com.benkih.estore.ledger.enums.LedgerEntryDirection;
import com.benkih.estore.ledger.enums.LedgerEntryType;
import com.benkih.estore.ledger.enums.LedgerTransactionType;
import com.benkih.estore.ledger.service.LedgerAccountService;
import com.benkih.estore.ledger.service.LedgerService;
import com.benkih.estore.payout.dto.response.PayoutResponseDto;
import com.benkih.estore.payout.entity.Payout;
import com.benkih.estore.payout.enums.PayoutStatus;
import com.benkih.estore.payout.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PayoutService implements IPayoutService{

  private final PayoutRepository payoutRepository;
  private final LedgerService ledgerService;
  private final LedgerAccountService ledgerAccountService;
  private final BusinessBalanceService balanceService;
  private final BusinessRepository businessRepository;
  private final BankAccountRepository bankAccountRepository;

  public PayoutResponseDto requestPayout(
      Long businessId,
      String bankAccountSlug,
      CurrencyCode currency,
      BigDecimal amount,
      String idempotencyKey
  ) {

    Business business = businessRepository.findById(businessId)
        .orElseThrow(() ->
            new ResourceNotFoundException("Business not found")
        );

    BankAccount bankAccount = bankAccountRepository.findBySlugAndBusinessId(
            bankAccountSlug,
            business.getId()
        )
        .orElseThrow(() ->
            new ResourceNotFoundException("Bank account not found")
        );

    if (amount.signum() <= 0) {
      throw new BadRequestException("Payout amount must be greater than zero");
    }

 // Idempotency check
    Optional<Payout> existingPayout = payoutRepository.findByIdempotencyKey(idempotencyKey);

    if (existingPayout.isPresent()) {
      Payout payout = existingPayout.get();
      boolean sameRequest = payout.getBusiness().getId().equals(businessId)
              && payout.getBankAccount().getId()
              .equals(bankAccount.getId())
              && payout.getCurrency() == currency
              && payout.getNetAmount().compareTo(amount) == 0;

      if (!sameRequest) {
        throw new AlreadyExistsException(
            "Idempotency key has already been used " + "for a different payout request"
        );
      }
      return convertToDto(payout);
    }

    if (!bankAccount.isVerified()) {
      throw new BadRequestException("Bank account must be verified before requesting a payout");
    }

    BusinessBalance balance = balanceService.getOrCreate(
            business,
            currency
        );

    if (balance.getAvailableBalance().compareTo(amount) < 0) {
      throw new BadRequestException("Insufficient available balance");
    }

    Payout payout = new Payout();

    payout.setPayoutNumber(generatePayoutNumber());
    payout.setBusiness(business);
    payout.setBankAccount(bankAccount);
    payout.setCurrency(currency);
    payout.setNetAmount(amount);
    payout.setStatus(PayoutStatus.REQUESTED);
    payout.setIdempotencyKey(idempotencyKey);
    payout.setRequestedAt(Instant.now());

    payout.setAccountName(bankAccount.getAccountName());
    payout.setAccountNumber(bankAccount.getAccountNumber());
    payout.setBankCode(bankAccount.getBankCode());
    payout.setBankName(bankAccount.getBankName());

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
//                LedgerEntryType.SELLER_AVAILABLE,
                LedgerEntryDirection.DEBIT,
                amount,
                null,
                null,
                payout,
                "Seller payout"
            ),

            new LedgerPosting(
                payoutProcessing,
//                LedgerEntryType.PAYOUT_PROCESSING,
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

    payout = payoutRepository.save(payout);
    return convertToDto( payout);
  }


  @Transactional
  public void markPayoutSuccessful(
      Payout payout,
      String providerReference
  ) {

    if (payout.getStatus() != PayoutStatus.PROCESSING) {
      throw new IllegalStateException("Payout is not processing");
    }

    CurrencyCode currency = payout.getCurrency();

    BigDecimal amount = payout.getNetAmount();

    LedgerAccount payoutProcessing = ledgerAccountService.getOrCreatePlatformAccount(
            LedgerAccountType.PAYOUT_PROCESSING,
            currency
        );

    /*
     * In the complete ledger you would have a
     * platform cash/bank account as the other side.
     */
    LedgerAccount bank = ledgerAccountService.getOrCreatePlatformAccount(
            LedgerAccountType.PLATFORM_CASH,
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
//                LedgerEntryType.PAYOUT_PROCESSING,
                LedgerEntryDirection.DEBIT,
                amount,
                null,
                null,
                payout,
                "Clear payout processing"
            ),

            new LedgerPosting(
                bank,
//                LedgerEntryType.PLATFORM_REVENUE,
                LedgerEntryDirection.CREDIT,
                amount,
                null,
                null,
                payout,
                "Funds paid to seller"
            )
        )
    );

    payout.setStatus(PayoutStatus.SUCCESS);
    payout.setProviderReference(providerReference);
    payout.setProcessedAt(Instant.now());

    payoutRepository.save(payout);
  }

  public PayoutResponseDto convertToDto(Payout payout) {

    return PayoutResponseDto.builder()
        .slug(payout.getSlug())
        .payoutNumber(payout.getPayoutNumber())
        .businessSlug(payout.getBusiness().getSlug())
        .bankAccountSlug(payout.getBankAccount().getSlug())
        .accountName(payout.getAccountName())
        .accountNumber(maskAccountNumber(payout.getAccountNumber()))
        .bankCode(payout.getBankCode())
        .bankName(payout.getBankName())
        .grossAmount(payout.getGrossAmount())
        .transferFee(payout.getTransferFee())
        .stampDuty(payout.getStampDuty())
        .netAmount(payout.getNetAmount())
        .currency(payout.getCurrency())
        .status(payout.getStatus())
        .provider(payout.getProvider())
        .providerReference(payout.getProviderReference())
        .failureReason(payout.getFailureReason())
        .requestedAt(payout.getRequestedAt())
        .processedAt(payout.getProcessedAt())
        .build();
  }

  private String maskAccountNumber(String accountNumber) {
    if (accountNumber == null || accountNumber.length() <= 4) {
      return accountNumber;
    }

    return "*".repeat(accountNumber.length() - 4)
        + accountNumber.substring(accountNumber.length() - 4);
  }

  private String generatePayoutNumber() {
    return "PAY-" + UUID.randomUUID()
        .toString()
        .replace("-", "")
        .substring(0, 12)
        .toUpperCase();
  }
}
