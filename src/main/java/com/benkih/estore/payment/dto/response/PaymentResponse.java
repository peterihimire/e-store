package com.benkih.estore.payment.dto.response;

import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.enums.PaymentMethod;
import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.common.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String slug;

    private String reference;

    private String orderSlug;

    private BigDecimal amount;

    private CurrencyCode currency;

    private PaymentMethod paymentMethod;

    private PaymentProvider paymentProvider;

    private PaymentStatus paymentStatus;

    private String transactionId;

    private String gatewayResponse;

    private Instant paidAt;
}
