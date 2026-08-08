package com.benkih.estore.payment.dto.response;

import com.benkih.estore.common.enums.RefundGatewayStatus;
import com.benkih.estore.common.enums.RefundStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundPaymentResponse {

 //Whether the API call succeeded.
  private boolean success;

 //Gateway refund identifier.
  private String gatewayRefundId;

// Gateway transaction reference.
  private String transactionReference;

 // Refund status returned by gateway.
  private RefundGatewayStatus status;

// Gateway message.
  private String message;

  private LocalDateTime refundedAt;

  private String refundReference;

  private BigDecimal amount;

  private String currency;

  private String reason;

  private String createdAt;
}
