package com.benkih.estore.cart.service;


import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.cart.repository.CartRepository;
import com.benkih.estore.cart.entity.CartItem;
import com.benkih.estore.cart.repository.CartItemRepository;
import com.benkih.estore.common.exceptions.NotFoundException;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartItemService implements ICartItemService {
  private final CartItemRepository cartItemRepository;
  private final CartRepository cartRepository;
  private final IProductService productService;
  private final ICartService cartService;

  @Override
  public void addItemToCart(String cartSlug, String productSlug, int quantity) {
    Cart cart = cartService.getCart(cartSlug);
    Product product = productService.getProductBySlug(productSlug);
    CartItem cartItem = cart.getItems()
        .stream()
        .filter(item -> item.getProduct().getSlug().equals(productSlug))
        .findFirst().orElse(new CartItem());

    if(cartItem.getId() == null){
      cartItem.setCart(cart);
      cartItem.setProduct(product);
      cartItem.setQuatity(quantity);
      cartItem.setUnitPrice(product.getPrice());
    } else{
      cartItem.setQuatity(cartItem.getQuatity() + quantity);
    }
    cartItem.setTotalPrice();
    cart.addItem(cartItem);
    cartItemRepository.save(cartItem);
    cartRepository.save(cart);
  }

  @Override
  public void removeItemFromCart(String cartSlug, String productSlug) {
    Cart cart = cartService.getCart(cartSlug);
    CartItem itemToRemove = getCartItem(cartSlug, productSlug);
    cart.removeItem(itemToRemove);
    cartRepository.save(cart);
  }

  @Override
  public void updateItemQuantity(String cartSlug, String productSlug, int quantity) {
    Cart cart = cartService.getCart(cartSlug);
    cart.getItems()
        .stream().filter(item -> item.getProduct().getSlug().equals(productSlug))
        .findFirst()
        .ifPresent(item -> {
          item.setQuatity(quantity);
          item.setUnitPrice(item.getProduct().getPrice());
          item.setTotalPrice();
        });
    BigDecimal totalAmount = cart.getTotalAmount();
    cart.setTotalAmount(totalAmount);
    cartRepository.save(cart);
  }

  @Override
  public CartItem getCartItem(String cartSlug, String productSlug){
    Cart cart = cartService.getCart(cartSlug);
    return  cart.getItems()
          .stream()
          .filter(item -> item.getProduct().getSlug().equals(productSlug))
          .findFirst().orElseThrow(() -> new NotFoundException("Product not found."));
  }
}
