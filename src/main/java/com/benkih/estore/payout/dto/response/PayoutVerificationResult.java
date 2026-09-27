package com.benkih.estore.payout.dto.response;

import com.benkih.estore.payout.enums.PayoutGatewayStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class PayoutVerificationResult {

  private String providerReference;
  private String providerTransferCode;
  private PayoutGatewayStatus status;
  private BigDecimal amount;
  private String currency;
  private Instant transferredAt;
}
