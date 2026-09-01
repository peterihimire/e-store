package com.benkih.estore.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaystackVerifyResponse {

  private boolean status;

  private String message;

  private Data data;

  @Getter
  @Setter
  public static class Data {

    private Long id;

    private String status;

    private String reference;

    private Long amount;

    @JsonProperty("fees")
    private Long fees;

    @JsonProperty("gateway_response")
    private String gatewayResponse;

    @JsonProperty("paid_at")
    private Instant paidAt;

    private Authorization authorization;
  }

  @Getter
  @Setter
  public static class Authorization {

    @JsonProperty("authorization_code")
    private String authorizationCode;

    private String channel;

    private String bank;

    private String brand;
  }
}