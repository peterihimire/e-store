package com.benkih.estore.cart.service;

import com.benkih.estore.cart.entity.CartItem;

public interface ICartItemService {

  void addItemToCart(String cartSlug, String variantSlug, int quantity);

  void removeItemFromCart(String cartSlug, String variantSlug);

  void updateItemQuantity(String cartSlug, String variantSlug, int quantity);

  CartItem getCartItem(String cartSlug, String variantSlug);
}
