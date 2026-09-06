package com.benkih.estore.business.service;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.business.entity.BusinessBalance;
import com.benkih.estore.business.repository.BusinessBalanceRepository;
import com.benkih.estore.common.enums.CurrencyCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessBalanceService {
  private final BusinessBalanceRepository repository;

  public BusinessBalance getOrCreate(
      Business business,
      CurrencyCode currency
  ) {

    return repository
        .findByBusinessIdAndCurrency(
            business.getId(),
            currency
        )
        .orElseGet(() -> {

          BusinessBalance balance = new BusinessBalance();

          balance.setBusiness(business);
          balance.setCurrency(currency);
          balance.setUpdatedAt(Instant.now());

          return repository.save(balance);
        });
  }

  public void increasePending(
      Business business,
      CurrencyCode currency,
      BigDecimal amount
  ) {

    BusinessBalance balance =
        getOrCreate(business, currency);

    balance.setPendingBalance(
        balance.getPendingBalance().add(amount)
    );

    balance.setUpdatedAt(Instant.now());

    repository.save(balance);
  }

  public void movePendingToAvailable(
      Business business,
      CurrencyCode currency,
      BigDecimal amount
  ) {

    BusinessBalance balance =
        getOrCreate(business, currency);

    if (
        balance.getPendingBalance()
            .compareTo(amount) < 0
    ) {
      throw new IllegalStateException("Insufficient pending balance");
    }

    balance.setPendingBalance(
        balance.getPendingBalance().subtract(amount)
    );

    balance.setAvailableBalance(
        balance.getAvailableBalance().add(amount)
    );

    balance.setUpdatedAt(Instant.now());

    repository.save(balance);
  }

  public void moveAvailableToPayout(
      Business business,
      CurrencyCode currency,
      BigDecimal amount
  ) {

    BusinessBalance balance =
        getOrCreate(business, currency);

    if (
        balance.getAvailableBalance()
            .compareTo(amount) < 0
    ) {
      throw new IllegalStateException("Insufficient available balance");
    }

    balance.setAvailableBalance(
        balance.getAvailableBalance().subtract(amount)
    );

    balance.setUpdatedAt(Instant.now());

    repository.save(balance);
  }
}
