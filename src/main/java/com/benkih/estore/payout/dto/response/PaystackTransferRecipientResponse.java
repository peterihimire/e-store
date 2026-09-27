package com.benkih.estore.payout.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class PaystackTransferRecipientResponse {

  private boolean status;
  private String message;
  private Data data;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Data {

    private String domain;
    private String type;
    private String currency;
    private String name;
    private Details details;

    @JsonProperty("recipient_code")
    private String recipientCode;

    private boolean active;

    private Long id;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("updatedAt")
    private Instant updatedAt;

  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Details {

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("bank_code")
    private String bankCode;

    @JsonProperty("bank_name")
    private String bankName;
  }
}
