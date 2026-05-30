package com.benkih.estore.cart.service;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.cart.repository.CartRepository;
import com.benkih.estore.cart.repository.CartItemRepository;
import com.benkih.estore.common.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static java.util.Arrays.stream;
import java.util.UUID;

//String cartSlug = UUID.randomUUID().toString();

@Service
@RequiredArgsConstructor
public class CartService  implements ICartService{
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;

  @Override
  public Cart getCart(String slug) {
    Cart cart = cartRepository.findBySlug(slug)
        .orElseThrow(()-> new NotFoundException("Cart not found"));
    BigDecimal totalAmount = cart.getTotalAmount();
    cart.setTotalAmount(totalAmount);

    return cartRepository.save(cart);
  }

  @Override
  public void clearCart(String slug) {
    Cart cart = getCart(slug);
    cartItemRepository.deleteAllByCartSlug(slug);
    cart.getItems().clear();
    cartRepository.deleteBySlug(slug);


  }

  @Override
  public BigDecimal getTotalPrice(String slug) {
    Cart cart= getCart(slug);
    return cart.getTotalAmount();
  //  return cart.getItems()
  //      .stream()
  //      .map(CartItem :: getTotalPrice)
  //      .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  public String initializeNewCart(){
    Cart newCart = new Cart();
    String newCartSlug = UUID.randomUUID().toString();
    newCart.setSlug(newCartSlug);
    return cartRepository.save(newCart).getSlug();
  }
}
