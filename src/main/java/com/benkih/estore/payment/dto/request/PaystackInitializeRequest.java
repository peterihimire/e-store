package com.benkih.estore.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
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
public class PaystackInitializeRequest {

    private String email;

    private BigDecimal amount;

    private String reference;

    private String currency;

    @JsonProperty("callback_url")
    private String callbackUrl;
}
