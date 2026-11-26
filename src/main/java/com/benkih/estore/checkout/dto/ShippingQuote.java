package com.benkih.estore.checkout.dto;

import com.benkih.estore.common.enums.DeliveryMethod;
import java.math.BigDecimal;

public record ShippingQuote(
        BigDecimal amount, DeliveryMethod deliveryMethod, String zone, String rateCode) {}
// @Getter
// @AllArgsConstructor
// public class ShippingQuote {
//
//  private BigDecimal amount;
//
//  @Enumerated(EnumType.STRING)
//  @Column(
//      name = "delivery_method",
//      nullable = false,
//      length = 30
//  )
//  private DeliveryMethod deliveryMethod;
//
//  private String zone;
//
//  private String rateCode;
// }
