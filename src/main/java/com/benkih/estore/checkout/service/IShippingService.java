package com.benkih.estore.checkout.service;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.user.entity.Address;

import java.math.BigDecimal;

public interface IShippingService {

  BigDecimal calculateShipping(
      Cart cart,
      Address shippingAddress
  );
}