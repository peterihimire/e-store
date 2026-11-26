package com.benkih.estore.refund.dto.response;

import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.common.enums.RefundGatewayStatus;
import com.benkih.estore.common.enums.RefundStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private Instant createdAt;

    private Instant refundedAt;
}
