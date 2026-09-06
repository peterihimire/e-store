package com.benkih.estore.checkout.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

public record DiscountQuote(
    BigDecimal amount,
    String promotionCode,
    String promotionName
) {}
//@Getter
//@AllArgsConstructor
//public class DiscountQuote {
//
//  private BigDecimal amount;
//
//  private String promotionCode;
//
//  private String promotionName;
//}
