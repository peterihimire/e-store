package com.benkih.estore.payment.dto.response;

import com.benkih.estore.common.enums.RefundGatewayStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundPaymentResponse {

    // Whether the API call succeeded.
    private boolean success;

    // Gateway refund identifier.
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
