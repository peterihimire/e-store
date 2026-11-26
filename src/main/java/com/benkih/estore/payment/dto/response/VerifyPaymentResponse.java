package com.benkih.estore.payment.dto.response;

import com.benkih.estore.common.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private Instant paidAt;

    private BigDecimal processorFee;
}
