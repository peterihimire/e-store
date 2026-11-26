package com.benkih.estore.cart.controller;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.cart.service.CartItemService;
import com.benkih.estore.cart.service.CartService;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.service.UserService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@Tag(name = "Cart Items", description = "Adding, updating and removing cart items")
@RestController
@RequestMapping("${api.prefix}/cartItems")
public class CartItemController {
    private final CartItemService cartItemService;
    private final CartService cartService;
    private final UserService userService;

    @PostMapping("/item/add")
    public ResponseEntity<ApiResponse<Void>> addItemToCart(
            @RequestParam String variantSlug, @RequestParam Integer quantity) {
        //    log.info("Entered addItemToCart endpoint");
        //    log.info("cartSlug={}, productSlug={}, quantity={}", productSlug, quantity);
        try {
            User user = userService.getAuthenticatedUser();
            log.info("Lets see info user email={}", user.getEmail());
            Cart cart = cartService.initializeNewCart(user);

            cartItemService.addItemToCart(cart.getSlug(), variantSlug, quantity);
            return ResponseEntity.ok(new ApiResponse<>("success", "Product added to cart", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        } catch (JwtException e) {
            return ResponseEntity.status(UNAUTHORIZED)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @DeleteMapping("/cart/{cartSlug}/item/{productSlug}/remove")
    public ResponseEntity<ApiResponse<Void>> removeItemFromCart(
            @PathVariable String cartSlug, @PathVariable String productSlug) {
        try {
            cartItemService.removeItemFromCart(cartSlug, productSlug);
            return ResponseEntity.ok(
                    new ApiResponse<>("success", "Item removed successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @PutMapping("/cart/{cartSlug}/item/{productSlug}/update")
    public ResponseEntity<ApiResponse<Void>> updateItemQuantity(
            @PathVariable String cartSlug,
            @PathVariable String productSlug,
            @RequestParam Integer quantity) {
        try {
            cartItemService.updateItemQuantity(cartSlug, productSlug, quantity);
            return ResponseEntity.ok(new ApiResponse<>("success", "Update item successfull", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }
}
