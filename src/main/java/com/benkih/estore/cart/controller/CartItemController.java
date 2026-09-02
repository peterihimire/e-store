package com.benkih.estore.cart.controller;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.cart.service.ICartItemService;
import com.benkih.estore.cart.service.ICartService;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.service.IUserService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.jar.JarException;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/cartItems")
public class CartItemController {
  private final ICartItemService cartItemService;
  private final ICartService cartService;
  private final IUserService userService;

  @PostMapping("/item/add")
    public ResponseEntity<ApiResponse> addItemToCart(@RequestParam String variantSlug, @RequestParam Integer quantity){
  //    log.info("Entered addItemToCart endpoint");
  //    log.info("cartSlug={}, productSlug={}, quantity={}", productSlug, quantity);
    try {
      User user = userService.getAuthenticatedUser();
      log.info("Lets see info user email={}", user.getEmail());
      Cart cart = cartService.initializeNewCart(user);

      cartItemService.addItemToCart(cart.getSlug(), variantSlug, quantity);
      return ResponseEntity.ok(new ApiResponse("success","Product added to cart",null));
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail",e.getMessage(), null));
    } catch(JwtException e){
      return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }

  @DeleteMapping("/cart/{cartSlug}/item/{productSlug}/remove")
  public ResponseEntity<ApiResponse> removeItemFromCart(@PathVariable String cartSlug, @PathVariable String productSlug) {
    try {
      cartItemService.removeItemFromCart(cartSlug, productSlug);
      return ResponseEntity.ok(new ApiResponse("success","Item removed successfully", null));
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }

  @PutMapping("/cart/{cartSlug}/item/{productSlug}/update")
  public ResponseEntity<ApiResponse> updateItemQuantity(@PathVariable String cartSlug, @PathVariable String productSlug, @RequestParam Integer quantity){
    try {
      cartItemService.updateItemQuantity(cartSlug, productSlug, quantity);
      return ResponseEntity.ok(new ApiResponse("success","Update item successfull", null));
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }
}
