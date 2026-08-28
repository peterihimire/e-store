package com.benkih.estore.checkout.dto;


import com.benkih.estore.common.enums.DeliveryMethod;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ShippingQuote {

  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(
      name = "delivery_method",
      nullable = false,
      length = 30
  )
  private DeliveryMethod deliveryMethod;

  private String zone;

  private String rateCode;
}
