package com.benkih.estore.payout.service;

import com.benkih.estore.common.enums.CurrencyCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PayoutFeeService {
  private static final BigDecimal FIVE_THOUSAND = BigDecimal.valueOf(5_000);
  private static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10_000);
  private static final BigDecimal FIFTY_THOUSAND = BigDecimal.valueOf(50_000);
  private static final BigDecimal TEN_NAIRA = BigDecimal.valueOf(10);
  private static final BigDecimal TWENTY_FIVE_NAIRA = BigDecimal.valueOf(25);
  private static final BigDecimal FIFTY_NAIRA = BigDecimal.valueOf(50);

  public BigDecimal calculateTransferFee(
      BigDecimal amount,
      CurrencyCode currency
  ) {
    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException(
          "Payout amount must be greater than zero"
      );
    }

    if (currency != CurrencyCode.NGN) {
      throw new IllegalArgumentException(
          "Paystack transfer fees are currently configured for NGN"
      );
    }

    if (amount.compareTo(FIVE_THOUSAND) <= 0) {
      return TEN_NAIRA;
    }

    if (amount.compareTo(FIFTY_THOUSAND) <= 0) {
      return TWENTY_FIVE_NAIRA;
    }
    return FIFTY_NAIRA;
  }


  public BigDecimal calculateStampDuty(
      BigDecimal amount,
      CurrencyCode currency
  ) {

    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException(
          "Payout amount must be greater than zero"
      );
    }

    if (currency != CurrencyCode.NGN) {
      throw new IllegalArgumentException(
          "Stamp duty is currently configured for NGN"
      );
    }

    return amount.compareTo(TEN_THOUSAND) >= 0
        ? FIFTY_NAIRA
        : BigDecimal.ZERO;
  }
}
