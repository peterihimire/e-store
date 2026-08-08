package com.benkih.estore.payment.dto.webhook;

import com.benkih.estore.common.enums.PaymentProvider;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackWebhookEvent implements PaymentWebhookEvent{

  private String event;

  private DataPayload data;

  @Override
  public PaymentProvider provider() {
    return PaymentProvider.PAYSTACK;
  }

  @Override
  public String eventType() {
    return event;
  }

//  @Override
//  public String reference() {
//    return data.getReference();
//  }
//
//  @Override
//  public String transactionId() {
//    return String.valueOf(data.getId());
//  }

  @Override
  public String reference() {
    return data != null ? data.getReference() : null;
  }

  @Override
  public String transactionReference() {
    return data != null ? data.getTransactionReference() : null;
  }

  @Override
  public String refundReference() {
    return data != null ? data.getRefundReference() : null;
  }

  @Override
  public String transactionId() {
    return data != null && data.getId() != null
        ? String.valueOf(data.getId())
        : null;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class DataPayload {

    private Long id;

    private String reference;

    @JsonProperty("transaction_reference")
    private String transactionReference;

    @JsonProperty("refund_reference")
    private String refundReference;

    private String status;

    private Integer amount;

    private String currency;

    private String reason;

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