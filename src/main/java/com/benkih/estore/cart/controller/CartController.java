package com.benkih.estore.cart.controller;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.benkih.estore.cart.dto.response.CartResponseDto;
import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.cart.service.CartService;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.security.user.StoreUserDetails;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Tag(name = "Carts", description = "Current user's shopping cart")
@RestController
@RequestMapping("${api.prefix}/carts")
public class CartController {
    private final CartService cartService;
    private final UserService userService;

    @GetMapping("/my-cart")
    public ResponseEntity<ApiResponse<CartResponseDto>> getCart(
            @AuthenticationPrincipal StoreUserDetails userDetails) {
        try {
            //  User user = getAu
            CartResponseDto cartDto = cartService.getCartForCurrentUser(userDetails.getSlug());
            //      CartResponseDto cartData = cartService.getConvertedCart(cart);
            return ResponseEntity.ok(new ApiResponse<>("success", "Cart returned", cartDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal StoreUserDetails userDetails) {
        try {
            Cart cart = cartService.getCartByUserSlug(userDetails.getSlug());
            cartService.clearCart(cart.getSlug());
            return ResponseEntity.ok(
                    new ApiResponse<>("success", "Cart cleared successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @GetMapping("/total-price")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalAmount(
            @AuthenticationPrincipal StoreUserDetails userDetails) {
        try {
            Cart cart = cartService.getCartByUserSlug(userDetails.getSlug());
            BigDecimal totalPrice = cartService.getTotalPrice(cart.getSlug());
            return ResponseEntity.ok(
                    new ApiResponse<>("success", "Total Price Returned", totalPrice));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    // Should be used in Services, utility classes and non controller classes
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.getUserBySlug(email);
    }
}
