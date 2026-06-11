package com.benkih.estore.cart.service;

import com.benkih.estore.cart.dto.response.CartResponseDto;
import com.benkih.estore.cart.entity.Cart;

import java.math.BigDecimal;

public interface ICartService {
  Cart getCart(String slug);

  CartResponseDto getConvertedCart(Cart cart);

  void clearCart(String slug);
  BigDecimal getTotalPrice(String slug);

  String initializeNewCart();
}
