package com.benkih.estore.order.dto.request;

import com.benkih.estore.common.enums.DeliveryMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderRequest {
  private String shippingAddressSlug;

  private String billingAddressSlug;

  private String couponCode;

  private DeliveryMethod deliveryMethod;
}
