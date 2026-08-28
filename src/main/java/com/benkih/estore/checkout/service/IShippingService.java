package com.benkih.estore.checkout.service;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.checkout.dto.ShippingQuote;
import com.benkih.estore.common.enums.DeliveryMethod;
import com.benkih.estore.user.entity.Address;

import java.math.BigDecimal;

public interface IShippingService {

  BigDecimal calculateShipping(
      Cart cart,
      Address shippingAddress
  );

//  ShippingQuote quote(Cart cart, Address address, String deliveryMethod);

  ShippingQuote quote(Cart cart, Address address, DeliveryMethod deliveryMethod);
}