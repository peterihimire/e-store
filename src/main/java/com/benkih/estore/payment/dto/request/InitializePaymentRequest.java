package com.benkih.estore.payment.dto.request;

import com.benkih.estore.common.enums.CurrencyCode;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class InitializePaymentRequest {

    private String email;

    private BigDecimal amount;

    private CurrencyCode currency;

    private String reference;

    private String callbackUrl;
}
