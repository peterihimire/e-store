package com.benkih.estore.checkout.service;

import com.benkih.estore.cart.entity.Cart;

import java.math.BigDecimal;

public interface IDiscountService {

  BigDecimal calculateDiscount(
      Cart cart,
      BigDecimal subtotal
  );
}
