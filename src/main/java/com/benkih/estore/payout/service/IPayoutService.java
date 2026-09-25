package com.benkih.estore.payout.service;

import com.benkih.estore.business.entity.BankAccount;
import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.payout.dto.response.PayoutResponseDto;
import com.benkih.estore.payout.entity.Payout;

import java.math.BigDecimal;

public interface IPayoutService {

  PayoutResponseDto requestPayout(
      Long businessId,
      String bankAccountSlug,
      CurrencyCode currency,
      BigDecimal amount,
      String IdempotencyKey
  );

  void markPayoutSuccessful(
      Payout payout,
      String providerReference);
}
