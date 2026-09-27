package com.benkih.estore.payout.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaystackTransferRequest {
  private String source;
  private Long amount;
  private String recipient;
  private String reference;
  private String reason;
  private String currency;
}
