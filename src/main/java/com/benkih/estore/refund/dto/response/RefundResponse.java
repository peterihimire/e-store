package com.benkih.estore.refund.dto.response;

import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.common.enums.RefundGatewayStatus;
import com.benkih.estore.common.enums.RefundStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

  private CurrencyCode currency;

  private RefundStatus refundStatus;

  private RefundGatewayStatus gatewayStatus;

  private String reason;

  private String failureReason;

  private String gatewayRefundId;

  private PaymentProvider provider;

  private List<RefundItemResponse> items;

  private LocalDateTime createdAt;

  private LocalDateTime refundedAt;
}
