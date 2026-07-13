package com.benkih.estore.payment.dto.response;

import com.benkih.estore.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyPaymentResponse {

  private String transactionId;

  private String reference;

  private PaymentStatus status;

  private BigDecimal amount;

  private String gatewayResponse;

  private String authorizationCode;

  private LocalDateTime paidAt;
}
