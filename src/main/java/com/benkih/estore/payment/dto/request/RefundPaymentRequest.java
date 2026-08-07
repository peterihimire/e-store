package com.benkih.estore.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class RefundPaymentRequest {

  /**
   * Original payment transaction reference.
   * e.g. PAYSTACK_REFERENCE
   */
  @JsonProperty("transaction")
  private String transactionReference;

  private BigDecimal amount;

  /**
   * Reason shown on the gateway dashboard.
   */
  private String reason;

}