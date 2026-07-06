package com.benkih.estore.cart.controller;

import com.benkih.estore.cart.dto.response.CartResponseDto;
import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.cart.service.ICartService;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.security.user.StoreUserDetails;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/carts")
public class CartController {
  private final ICartService cartService;
  private final IUserService userService;

  @GetMapping("/get-cart")
  public ResponseEntity<ApiResponse> getCart(@AuthenticationPrincipal StoreUserDetails userDetails){
    try {
      //  User user = getAu
      CartResponseDto cart = cartService.getCartForCurrentUser(userDetails.getSlug());
//      CartResponseDto cartData = cartService.getConvertedCart(cart);
      return ResponseEntity.ok(new ApiResponse("success","Cart returned", cart));
    } catch (ResourceNotFoundException e) {
      return  ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }

  @DeleteMapping("{cartSlug}/clear")
  public ResponseEntity<ApiResponse> clearCart(@PathVariable String cartSlug){
    try {
      cartService.clearCart(cartSlug);
      return ResponseEntity.ok(new ApiResponse("success","Cart cleared successfully", null));
    } catch (ResourceNotFoundException e) {
      return  ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }

  @GetMapping("{cartSlug}/total-price")
  public ResponseEntity<ApiResponse> getTotalAmount(@PathVariable String cartSlug){
    try {
      BigDecimal totalPrice = cartService.getTotalPrice(cartSlug);
      return ResponseEntity.ok(new ApiResponse("success","Total Price Returned", totalPrice));
    } catch (ResourceNotFoundException e) {
      return  ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }

  private User getAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return userService.getUserBySlug(email);
  }

}
