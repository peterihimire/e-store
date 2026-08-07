package com.benkih.estore.refund.dto.response;

import com.benkih.estore.common.enums.Currency;
import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.common.enums.RefundStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse {

  private String slug;

  private String reference;

  private String orderSlug;

  private String paymentReference;

  private BigDecimal amount;

  private Currency currency;

  private RefundStatus status;

  private String reason;

  private String gatewayRefundId;

  private PaymentProvider provider;

  private LocalDateTime createdAt;

  private LocalDateTime refundedAt;
}
