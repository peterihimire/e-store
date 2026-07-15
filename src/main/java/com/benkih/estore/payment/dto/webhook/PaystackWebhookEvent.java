package com.benkih.estore.payment.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackWebhookEvent {

  private String event;

  private DataPayload data;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class DataPayload {

    private Long id;

    private String reference;

    private String status;

    /**
     * Amount in kobo.
     */
    private Integer amount;

    @JsonProperty("gateway_response")
    private String gatewayResponse;

    @JsonProperty("paid_at")
    private OffsetDateTime paidAt;

    private Authorization authorization;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Authorization {

    @JsonProperty("authorization_code")
    private String authorizationCode;
  }
}