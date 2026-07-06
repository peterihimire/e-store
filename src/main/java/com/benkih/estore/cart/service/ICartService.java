package com.benkih.estore.cart.service;

import com.benkih.estore.cart.dto.response.CartResponseDto;
import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface ICartService {
  Cart getCart(String slug);

  CartResponseDto getConvertedCart(Cart cart);

  void clearCart(String slug);
  BigDecimal getTotalPrice(String slug);

  Cart initializeNewCart(User user);

  Cart getCartByUserSlug(String userSlug);

  @Transactional
  CartResponseDto getCartForCurrentUser(String slug);
}
