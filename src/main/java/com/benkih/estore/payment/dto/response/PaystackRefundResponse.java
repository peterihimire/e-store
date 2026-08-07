package com.benkih.estore.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackRefundResponse {
  private boolean status;

  private String message;

  private Data data;

  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Data {
    private Long id;

    @JsonProperty("transaction_reference")
    private String transactionReference;

    @JsonProperty("refund_reference")
    private String refundReference;

    private String status;

    private Long amount;

    private String currency;

    private String reason;

    @JsonProperty("created_at")
    private String createdAt;

  }
}
