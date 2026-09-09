package com.benkih.estore.ledger.service;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.ledger.entity.LedgerAccount;
import com.benkih.estore.ledger.enums.LedgerAccountType;
import com.benkih.estore.ledger.repository.LedgerAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LedgerAccountService implements ILedgerAccountService{
  private final LedgerAccountRepository repository;

  public LedgerAccount getOrCreateSellerAccount(
      Business business,
      LedgerAccountType type,
      CurrencyCode currency
  ) {

    return repository.findByBusinessIdAndAccountTypeAndCurrency(
            business.getId(),
            type,
            currency
        )
        .orElseGet(() -> {

          LedgerAccount account = new LedgerAccount(
                  business,
                  type,
                  currency
              );

          return repository.save(account);
        });
  }


  public LedgerAccount getOrCreatePlatformAccount(
      LedgerAccountType type,
      CurrencyCode currency
  ) {

    return repository.findByBusinessIsNullAndAccountTypeAndCurrency(
            type,
            currency
        )
        .orElseGet(() -> {
          LedgerAccount account = new LedgerAccount(
                  null,
                  type,
                  currency
              );

          return repository.save(account);
        });
  }
}
