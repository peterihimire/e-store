package com.benkih.estore.cart.controller;

import com.benkih.estore.cart.service.ICartItemService;
import com.benkih.estore.cart.service.ICartService;
import com.benkih.estore.common.exceptions.NotFoundException;
import com.benkih.estore.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/cartItems")
public class CartItemController {
  private final ICartItemService cartItemService;
  private final ICartService cartService;

  @PostMapping("/item/add")
  public ResponseEntity<ApiResponse> addItemToCart(
      @RequestParam(required = false) String cartSlug,
      @RequestParam String productSlug,
      @RequestParam Integer quantity){
    log.info("Entered addItemToCart endpoint");
    log.info("cartSlug={}, productSlug={}, quantity={}",
        cartSlug,
        productSlug,
        quantity);
    try {
      if(cartSlug == null || cartSlug.isBlank()){
       cartSlug = cartService.initializeNewCart();
       log.info("Generated cart slug: {}", cartSlug);
       // System.out.println("I'm using java function here:", + cartSlug);
      }
      cartItemService.addItemToCart(cartSlug, productSlug,quantity);
      return ResponseEntity.ok(new ApiResponse("Product added to cart",null));
    } catch (NotFoundException e) {
      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @DeleteMapping("/cart/{cartSlug}/item/{productSlug}/remove")
  public ResponseEntity<ApiResponse> removeItemFromCart(@PathVariable String cartSlug, @PathVariable String productSlug) {
    try {
      cartItemService.removeItemFromCart(cartSlug, productSlug);
      return ResponseEntity.ok(new ApiResponse("Item removed successfully", null));
    } catch (NotFoundException e) {
      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @PutMapping("/cart/{cartSlug}/item/{productSlug}/update")
  public ResponseEntity<ApiResponse> updateItemQuantity(@PathVariable String cartSlug, @PathVariable String productSlug, @RequestParam Integer quantity){
    try {
      cartItemService.updateItemQuantity(cartSlug, productSlug, quantity);
      return ResponseEntity.ok(new ApiResponse("Update item successfull", null));
    } catch (NotFoundException e) {
      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }
}
