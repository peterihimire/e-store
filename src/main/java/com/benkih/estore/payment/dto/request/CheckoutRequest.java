package com.benkih.estore.payment.dto.request;

import com.benkih.estore.common.enums.PaymentMethod;
import com.benkih.estore.common.enums.PaymentProvider;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CheckoutRequest {
  private String orderSlug;

  private PaymentMethod paymentMethod;

  private PaymentProvider paymentProvider;
}
