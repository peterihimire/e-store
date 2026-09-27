package com.benkih.estore.payout.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaystackTransferRecipientRequest {

  private String type;

  private String name;

  @JsonProperty("account_number")
  private String accountNumber;

  @JsonProperty("bank_code")
  private String bankCode;

  private String currency;
}
