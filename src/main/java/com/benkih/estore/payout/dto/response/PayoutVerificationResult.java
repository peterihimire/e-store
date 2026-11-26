package com.benkih.estore.payout.dto.response;

import com.benkih.estore.payout.enums.PayoutGatewayStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PayoutVerificationResult {

    private String providerReference;
    private String providerTransferCode;
    private PayoutGatewayStatus status;
    private BigDecimal amount;
    private String currency;
    private Instant transferredAt;
}
