package com.benkih.estore.checkout.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;



@Slf4j
@Service
@RequiredArgsConstructor
public class TaxService implements ITaxService{
  private static final BigDecimal TAX_RATE = new BigDecimal("0.075");

  public BigDecimal calculate(BigDecimal taxableAmount) {
    if (taxableAmount == null || taxableAmount.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    return taxableAmount
        .multiply(TAX_RATE)
        .setScale(2, RoundingMode.HALF_UP);
  }
}
