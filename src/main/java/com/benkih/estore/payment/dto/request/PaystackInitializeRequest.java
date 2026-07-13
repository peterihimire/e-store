package com.benkih.estore.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaystackInitializeRequest {

  private String email;

  private BigDecimal amount;

  private String reference;

  private String currency;

  @JsonProperty("callback_url")
  private String callbackUrl;

}
