package com.benkih.estore.cart.service;

import com.benkih.estore.cart.dto.response.CartItemResponseDto;
import com.benkih.estore.cart.dto.response.CartResponseDto;
import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.cart.repository.CartRepository;
import com.benkih.estore.cart.repository.CartItemRepository;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static java.util.Arrays.stream;

import java.util.List;
import java.util.UUID;

//String cartSlug = UUID.randomUUID().toString();

@Service
@RequiredArgsConstructor
public class CartService  implements ICartService{
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductService productService;

  @Override
  public Cart getCart(String slug) {
    Cart cart = cartRepository.findBySlug(slug)
        .orElseThrow(()-> new ResourceNotFoundException("Cart not found"));
    BigDecimal totalAmount = cart.getTotalAmount();
    cart.setTotalAmount(totalAmount);

    return cartRepository.save(cart);
  }

  @Override
  public CartResponseDto getConvertedCart(Cart cart){
    List<CartItemResponseDto> items = cart.getItems()
        .stream()
        .map(item -> new CartItemResponseDto(
            item.getQuantity(),
            item.getUnitPrice(),
            item.getTotalPrice(),
            productService.convertToDto(item.getProduct())
        )).toList();
    return new CartResponseDto(
        cart.getSlug(),
        cart.getTotalAmount(),
       items
    );
  }

  @Transactional
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

  @Override
  public Cart getCartByUserSlug(String slug){
    return cartRepository.findByUserSlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
  }
}
