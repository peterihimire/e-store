package com.benkih.estore.cart.service;

import com.benkih.estore.cart.entity.CartItem;

public interface ICartItemService {
  void addItemToCart(String cartSlug, String productSlug, int quantity);
  void removeItemFromCart(String cartSlug, String productSlug);
  void updateItemQuantity(String cartSlug, String productSlug, int quantity);

  CartItem getCartItem(String cartSlug, String productSlug);
}
