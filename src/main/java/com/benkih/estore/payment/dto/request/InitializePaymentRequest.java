package com.benkih.estore.payment.dto.request;

import com.benkih.estore.common.enums.CurrencyCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
public class InitializePaymentRequest {

  private String email;

  private BigDecimal amount;

  private CurrencyCode currency;

  private String reference;

  private String callbackUrl;

}
