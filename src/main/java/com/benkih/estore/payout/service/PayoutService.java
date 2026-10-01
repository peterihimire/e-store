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
import com.benkih.estore.common.exceptions.PayoutGatewayException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.ledger.dto.LedgerPosting;
import com.benkih.estore.ledger.entity.LedgerAccount;
import com.benkih.estore.ledger.enums.LedgerAccountType;
import com.benkih.estore.ledger.enums.LedgerEntryDirection;
import com.benkih.estore.ledger.enums.LedgerEntryType;
import com.benkih.estore.ledger.enums.LedgerTransactionType;
import com.benkih.estore.ledger.service.LedgerAccountService;
import com.benkih.estore.ledger.service.LedgerService;
import com.benkih.estore.payout.dto.response.PayoutRecipientResult;
import com.benkih.estore.payout.dto.response.PayoutResponseDto;
import com.benkih.estore.payout.dto.response.PayoutResult;
import com.benkih.estore.payout.entity.Payout;
import com.benkih.estore.payout.entity.PayoutRecipient;
import com.benkih.estore.payout.enums.PayoutProvider;
import com.benkih.estore.payout.enums.PayoutStatus;
import com.benkih.estore.payout.provider.PayoutGateway;
import com.benkih.estore.payout.provider.PayoutGatewayFactory;
import com.benkih.estore.payout.repository.PayoutRecipientRepository;
import com.benkih.estore.payout.repository.PayoutRepository;
import com.benkih.estore.webhook.handler.WebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PayoutService implements IPayoutService{
//  @Value("${payout.provider}")
//  private PayoutProvider payoutProvider;
  private final PayoutProperties payoutProperties;
  private final PayoutRepository payoutRepository;
  private final PayoutRecipientRepository payoutRecipientRepository;
  private final LedgerService ledgerService;
  private final LedgerAccountService ledgerAccountService;
  private final BusinessBalanceService balanceService;
  private final BusinessRepository businessRepository;
  private final BankAccountRepository bankAccountRepository;
  private final PayoutGatewayFactory gatewayFactory;
  private final PayoutFeeService payoutFeeService;


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
              && payout.getAmount().compareTo(amount) == 0;

      if (!sameRequest) {
        throw new AlreadyExistsException("Idempotency key has already been used " + "for a different payout request");
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

    BigDecimal transferFee =
        payoutFeeService.calculateTransferFee(amount, currency);

    BigDecimal stampDuty =
        payoutFeeService.calculateStampDuty(amount, currency);

    Payout payout = createPayout(
        business,
        bankAccount,
        currency,
        amount,
        transferFee,
        stampDuty,
        idempotencyKey
    );

//    PayoutGateway gateway = gatewayFactory.get(payoutProvider);
    PayoutGateway gateway =
        gatewayFactory.get(payoutProperties.getProvider());

    PayoutRecipient recipient = resolvePayoutRecipient(payout, gateway);

    reservePayoutFunds(
        payout,
        business,
        currency,
        amount
    );

    try {
      PayoutResult result = gateway.initiate(payout, recipient);

      if (!result.isAccepted()) {
        handlePayoutInitiationFailure(
            payout,
            business,
            currency,
            amount,
            result.getFailureReason()
        );

        throw new PayoutGatewayException("Payout initiation failed: " + result.getFailureReason());
      }

      payout.setProviderReference(result.getProviderReference());
      payout.setStatus(PayoutStatus.PROCESSING);
      payout = payoutRepository.save(payout);

      return convertToDto(payout);
    } catch (PayoutGatewayException ex) {
      throw ex;
    } catch (Exception ex) {

      payout.setStatus(PayoutStatus.PROCESSING);
      payout.setFailureReason("Transfer status could not be confirmed");

      payoutRepository.save(payout);

      throw new PayoutGatewayException("Unable to confirm payout status. " + "The payout will be reconciled.", ex);
    }
//    return convertToDto( payout);
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

    BigDecimal amount = payout.getAmount();

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

  @Transactional
  public void handleWebhook(
      WebhookEvent event,
      String signature,
      String payload
  ) {

//    if (!isPayoutEvent(event)) {
//      throw new IllegalArgumentException(
//          "Invalid payout webhook event: " + event.eventType()
//      );
//    }

    Payout payout =
        payoutRepository.findByProviderReference(event.reference())
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Payout not found for provider reference: "
                        + event.reference()
                )
            );

//    if (isDuplicateWebhook(payout, event, payload)) {
//      log.info(
//          "Payout webhook already processed: {}",
//          event.reference()
//      );
//      return;
//    }

//    saveWebhookEvent(
//        payout,
//        event.eventType(),
//        payload,
//        signature
//    );

    switch (event.eventType()) {

      case "transfer.success":
        handleTransferSuccess(payout, event);
        break;

      case "transfer.failed":
        handleTransferFailure(payout, event);
        break;

      case "transfer.reversed":
        handleTransferReversed(payout, event);
        break;

      default:
        log.info(
            "Ignoring payout event: {}",
            event.eventType()
        );
    }
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
        .amount(payout.getAmount())
        .transferFee(payout.getTransferFee())
        .stampDuty(payout.getStampDuty())
//        .netAmount(payout.getNetAmount())
        .currency(payout.getCurrency())
        .status(payout.getStatus())
        .provider(payout.getProvider())
        .providerReference(payout.getProviderReference())
        .failureReason(payout.getFailureReason())
        .requestedAt(payout.getRequestedAt())
        .processedAt(payout.getProcessedAt())
        .build();
  }

  private Payout createPayout(
      Business business,
      BankAccount bankAccount,
      CurrencyCode currency,
      BigDecimal amount,
      BigDecimal transferFee,
      BigDecimal stampDuty,
      String idempotencyKey
  ) {

    Payout payout = new Payout();

    payout.setPayoutNumber(generatePayoutNumber());
    payout.setBusiness(business);
    payout.setBankAccount(bankAccount);
    payout.setCurrency(currency);
    payout.setProvider(payoutProperties.getProvider());

    payout.setAmount(amount);
    payout.setTransferFee(transferFee);
    payout.setStampDuty(stampDuty);

    payout.setStatus(PayoutStatus.REQUESTED);
    payout.setIdempotencyKey(idempotencyKey);
    payout.setRequestedAt(Instant.now());

    payout.setAccountName(bankAccount.getAccountName());
    payout.setAccountNumber(bankAccount.getAccountNumber());
    payout.setBankCode(bankAccount.getBankCode());
    payout.setBankName(bankAccount.getBankName());

    return payoutRepository.save(payout);
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

  private PayoutRecipient resolvePayoutRecipient(
      Payout payout,
      PayoutGateway gateway
  ) {

    return payoutRecipientRepository
        .findByBankAccountIdAndProviderAndActiveTrue(
            payout.getBankAccount().getId(),
            payout.getProvider()
        )
        .orElseGet(() -> createPayoutRecipient(payout, gateway));
  }

  private PayoutRecipient createPayoutRecipient(
      Payout payout,
      PayoutGateway gateway
  ) {

    PayoutRecipientResult result = gateway.createRecipient(payout.getBankAccount());

    if (!result.isSuccess()) {
      throw new PayoutGatewayException(
          "Unable to create payout recipient: "
              + result.getMessage()
      );
    }

    PayoutRecipient recipient = new PayoutRecipient();

    recipient.setBankAccount(payout.getBankAccount());
    recipient.setProvider(payout.getProvider());
    recipient.setRecipientCode(result.getRecipientCode());
    recipient.setRecipientName(result.getRecipientName());
    recipient.setActive(true);

    return payoutRecipientRepository.save(recipient);
  }

  private void reservePayoutFunds(
      Payout payout,
      Business business,
      CurrencyCode currency,
      BigDecimal amount
  ) {

    LedgerAccount available =
        ledgerAccountService.getOrCreateSellerAccount(
            business,
            LedgerAccountType.SELLER_AVAILABLE,
            currency
        );

    LedgerAccount payoutProcessing =
        ledgerAccountService.getOrCreatePlatformAccount(
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
                LedgerEntryDirection.DEBIT,
                amount,
                null,
                null,
                payout,
                "Seller payout"
            ),

            new LedgerPosting(
                payoutProcessing,
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

    payoutRepository.save(payout);
  }

  private void handlePayoutInitiationFailure(
      Payout payout,
      Business business,
      CurrencyCode currency,
      BigDecimal amount,
      String failureReason
  ) {

    LedgerAccount payoutProcessing =
        ledgerAccountService.getOrCreatePlatformAccount(
            LedgerAccountType.PAYOUT_PROCESSING,
            currency
        );

    LedgerAccount available =
        ledgerAccountService.getOrCreateSellerAccount(
            business,
            LedgerAccountType.SELLER_AVAILABLE,
            currency
        );

    ledgerService.post(
        LedgerTransactionType.PAYOUT_FAILED,
        currency,
        "PAYOUT-FAILED-" + payout.getSlug(),
        "Payout initiation failed",
        List.of(

            new LedgerPosting(
                payoutProcessing,
                LedgerEntryDirection.DEBIT,
                amount,
                null,
                null,
                payout,
                "Reverse failed payout"
            ),

            new LedgerPosting(
                available,
                LedgerEntryDirection.CREDIT,
                amount,
                null,
                null,
                payout,
                "Return seller payout funds"
            )
        )
    );

    balanceService.movePayoutToAvailable(
        business,
        currency,
        amount
    );

    payout.setStatus(PayoutStatus.FAILED);
    payout.setFailureReason(failureReason);
    payout.setProcessedAt(Instant.now());

    payoutRepository.save(payout);
  }

  private void handleTransferSuccess(
      Payout payout,
      WebhookEvent event
  ) {
    if (payout.getStatus() == PayoutStatus.COMPLETED) {
      return;
    }

    LedgerAccount processing =
        ledgerAccountService.getOrCreatePlatformAccount(
            LedgerAccountType.PAYOUT_PROCESSING,
            payout.getCurrency()
        );

    LedgerAccount cash =
        ledgerAccountService.getOrCreatePlatformAccount(
            LedgerAccountType.PLATFORM_CASH,
            payout.getCurrency()
        );

    ledgerService.post(
        LedgerTransactionType.PAYOUT_COMPLETED,
        payout.getCurrency(),
        "PAYOUT-COMPLETED-" + payout.getSlug(),
        "Payout completed",
        List.of(
            new LedgerPosting(
                processing,
                LedgerEntryDirection.DEBIT,
                payout.getAmount(),
                null,
                null,
                payout,
                "Complete payout processing"
            ),
            new LedgerPosting(
                cash,
                LedgerEntryDirection.CREDIT,
                payout.getAmount(),
                null,
                null,
                payout,
                "Payout completed"
            )
        )
    );

    payout.setStatus(PayoutStatus.COMPLETED);
    payout.setProcessedAt(Instant.now());

    payoutRepository.save(payout);
  }


  private void handleTransferFailure(
      Payout payout,
      WebhookEvent event
  ) {

    if (payout.getStatus() == PayoutStatus.FAILED) {
      return;
    }

    reversePayoutReservation(payout);

    payout.setStatus(PayoutStatus.FAILED);
    payout.setFailureReason(
        "Transfer failed"
    );
    payout.setProcessedAt(Instant.now());

    payoutRepository.save(payout);
  }


  private void handleTransferReversed(
      Payout payout,
      WebhookEvent event
  ) {

    if (payout.getStatus() == PayoutStatus.REVERSED) {
      return;
    }

    reversePayoutReservation(payout);

    payout.setStatus(PayoutStatus.REVERSED);
    payout.setFailureReason(
        "Transfer was reversed by payment provider"
    );
    payout.setProcessedAt(Instant.now());

    payoutRepository.save(payout);
  }

  private void reversePayoutReservation(Payout payout) {

    LedgerAccount payoutProcessing =
        ledgerAccountService.getOrCreatePlatformAccount(
            LedgerAccountType.PAYOUT_PROCESSING,
            payout.getCurrency()
        );

    LedgerAccount available =
        ledgerAccountService.getOrCreateSellerAccount(
            payout.getBusiness(),
            LedgerAccountType.SELLER_AVAILABLE,
            payout.getCurrency()
        );

    ledgerService.post(
        LedgerTransactionType.PAYOUT_FAILED,
        payout.getCurrency(),
        "PAYOUT-FAILED-" + payout.getSlug(),
        "Payout reservation reversed",
        List.of(
            new LedgerPosting(
                payoutProcessing,
                LedgerEntryDirection.DEBIT,
                payout.getAmount(),
                null,
                null,
                payout,
                "Reverse payout processing"
            ),
            new LedgerPosting(
                available,
                LedgerEntryDirection.CREDIT,
                payout.getAmount(),
                null,
                null,
                payout,
                "Return payout funds to seller"
            )
        )
    );

    balanceService.movePayoutToAvailable(
        payout.getBusiness(),
        payout.getCurrency(),
        payout.getAmount()
    );
  }
}
