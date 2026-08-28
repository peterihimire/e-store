package com.benkih.estore.checkout.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DiscountQuote {

  private BigDecimal amount;

  private String promotionCode;

  private String promotionName;
}
