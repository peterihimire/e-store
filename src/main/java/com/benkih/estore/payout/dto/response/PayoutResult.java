package com.benkih.estore.payout.dto.response;

import com.benkih.estore.payout.enums.PayoutGatewayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PayoutResult {
  private boolean accepted;
  private PayoutGatewayStatus status;
  private String providerReference;
  private String providerTransferCode;
  private String failureReason;
}
