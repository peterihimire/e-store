package com.benkih.estore.checkout.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TaxQuote {
  private BigDecimal amount;

  private BigDecimal rate;

  private String jurisdiction;

  private String taxType;

  private String ruleCode;
}
