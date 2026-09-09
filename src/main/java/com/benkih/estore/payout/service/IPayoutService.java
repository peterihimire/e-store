package com.benkih.estore.payout.service;

import com.benkih.estore.business.entity.BankAccount;
import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.payout.entity.Payout;

import java.math.BigDecimal;

public interface IPayoutService {

  Payout requestPayout(
      Business business,
      BankAccount bankAccount,
      CurrencyCode currency,
      BigDecimal amount,
      String idempotencyKey);

  void markPayoutSuccessful(
      Payout payout,
      String providerReference);
}
