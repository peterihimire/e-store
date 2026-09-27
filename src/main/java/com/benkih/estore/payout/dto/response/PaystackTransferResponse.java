package com.benkih.estore.payout.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class PaystackTransferResponse {

  private boolean status;
  private String message;
  private Data data;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Data {
    private Long id;
    private Long amount;
    private String currency;
    private String reference;
    private String source;
    private String reason;
    private String status;

    @JsonProperty("transfer_code")
    private String transferCode;

    private Object failures;

    @JsonProperty("transferred_at")
    private Instant transferredAt;

    private String domain;
    private Long integration;
    private Long request;
    private Long recipient;
    private Instant createdAt;
    private Instant updatedAt;
  }
}
