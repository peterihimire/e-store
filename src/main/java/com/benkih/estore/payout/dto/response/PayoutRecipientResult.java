package com.benkih.estore.payout.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PayoutRecipientResult {

  private boolean success;
  private String message;
  private String recipientCode;
  private String recipientName;
}
