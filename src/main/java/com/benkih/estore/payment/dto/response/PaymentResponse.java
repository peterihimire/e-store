package com.benkih.estore.payment.dto.response;

import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.enums.PaymentMethod;
import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.common.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

  private LocalDateTime paidAt;
}
