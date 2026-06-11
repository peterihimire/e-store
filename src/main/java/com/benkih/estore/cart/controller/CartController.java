package com.benkih.estore.cart.controller;

import com.benkih.estore.cart.dto.response.CartResponseDto;
import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.cart.service.ICartService;
import com.benkih.estore.common.exceptions.NotFoundException;
import com.benkih.estore.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/carts")
public class CartController {
  private final ICartService cartService;

  @GetMapping("/{cartSlug}")
  public ResponseEntity<ApiResponse> getCart(@PathVariable String cartSlug){
    try {
      Cart cart = cartService.getCart(cartSlug);
      CartResponseDto cartData = cartService.getConvertedCart(cart);
      return ResponseEntity.ok(new ApiResponse("Cart returned", cartData));
    } catch (NotFoundException e) {
      return  ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @DeleteMapping("{cartSlug}/clear")
  public ResponseEntity<ApiResponse> clearCart(@PathVariable String cartSlug){
    try {
      cartService.clearCart(cartSlug);
      return ResponseEntity.ok(new ApiResponse("Cart cleared successfully", null));
    } catch (NotFoundException e) {
      return  ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @GetMapping("{cartSlug}/total-price")
  public ResponseEntity<ApiResponse> getTotalAmount(@PathVariable String cartSlug){
    try {
      BigDecimal totalPrice = cartService.getTotalPrice(cartSlug);
      return ResponseEntity.ok(new ApiResponse("Total Price Returned", totalPrice));
    } catch (NotFoundException e) {
      return  ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

}
