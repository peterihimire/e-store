package com.benkih.estore.cart.service;

import com.benkih.estore.cart.dto.response.CartItemResponseDto;
import com.benkih.estore.cart.dto.response.CartResponseDto;
import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.cart.entity.CartItem;
import com.benkih.estore.cart.repository.CartRepository;
import com.benkih.estore.cart.repository.CartItemRepository;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.inventory.service.IInventoryService;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.service.IProductService;
import com.benkih.estore.security.user.StoreUserDetails;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static java.util.Arrays.stream;

import java.util.List;
import java.util.Map;
import java.util.Optional;

//String cartSlug = UUID.randomUUID().toString();
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService  implements ICartService{
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final IProductService productService;
  private final UserRepository userRepository;
  private final IInventoryService inventoryService;




  @Transactional // Don't use insert[save] in a read only method
  @Override
  public CartResponseDto getCartForCurrentUser(String slug) {
    User user = userRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Cart cart = cartRepository.findByUser(user);
    if (cart == null) {
      cart = new Cart();
      cart.setUser(user);
      cart = cartRepository.save(cart);
    }
    return getConvertedCart(cart);
  }

  @Override
  public Cart getCart(String slug) {
    return cartRepository.findBySlug(slug)
        .orElseThrow(()-> new ResourceNotFoundException("Cart not found"));

  }
  //    @Override
  //    public Cart getCart(String slug) {
  //      Cart cart = cartRepository.findBySlug(slug)
  //          .orElseThrow(()-> new ResourceNotFoundException("Cart not found"));
  //      BigDecimal totalAmount = cart.getTotalAmount();
  //      cart.setTotalAmount(totalAmount);
  //
  //      return cartRepository.save(cart);
  //    }

  @Transactional(readOnly = true)
  @Override
  public CartResponseDto getConvertedCart(Cart cart) {

    List<Product> products = cart.getItems()
        .stream()
        .map(CartItem::getProduct)
        .toList();

    List<CartItemResponseDto> items = cart.getItems()
        .stream()
        .map(item -> {

          Product product = item.getProduct();

          return new CartItemResponseDto(
              item.getQuantity(),
              item.getUnitPrice(),
              item.getTotalPrice(),
              productService.convertToDto(product)
          );
        })
        .toList();

    return new CartResponseDto(
        cart.getSlug(),
        cart.getTotalAmount(),
        items
    );
  }

//  @Transactional(readOnly = true)
//  @Override
//  public CartResponseDto getConvertedCart(Cart cart){
//    List<CartItemResponseDto> items = cart.getItems()
//        .stream()
//        .map(item -> {
//          Product product = item.getProduct();
//          // Fetch product with images in a new query
//          Product productWithImages = productService.getProductBySlug(product.getSlug());
//
//          return new CartItemResponseDto(
//              item.getQuantity(),
//              item.getUnitPrice(),
//              item.getTotalPrice(),
//              productService.convertToDto(productWithImages) // This will have images
//          );
//        })
//        .toList();
//
//    return new CartResponseDto(
//        cart.getSlug(),
//        cart.getTotalAmount(),
//        items
//    );
//  }

//  @Transactional(readOnly = true)
//  @Override
//  public CartResponseDto getConvertedCart(Cart cart){
//    cart.getItems().forEach(item -> {
//      System.out.println(item.getProduct().getClass());
//    });
//    List<CartItemResponseDto> items = cart.getItems()
//        .stream()
//        .map(item -> {
//          Product product = productService.getProductBySlug(
//              item.getProduct().getSlug()
//          );
//          return new CartItemResponseDto(
//              item.getQuantity(),
//              item.getUnitPrice(),
//              item.getTotalPrice(),
//              productService.convertToDto(product)
//          );
//        })
//        .toList();
//    //    List<CartItemResponseDto> items = cart.getItems()
//    //        .stream()
//    //        .map(item -> new CartItemResponseDto(
//    //            item.getQuantity(),
//    //            item.getUnitPrice(),
//    //            item.getTotalPrice(),
//    //            productService.convertToDto(item.getProduct())
//    //        )).toList();
//    return new CartResponseDto(
//        cart.getSlug(),
//        cart.getTotalAmount(),
//       items
//    );
//  }

  @Transactional
  @Override
  public void clearCart(String slug) {
    //    Cart cart = getCart(slug);
    //    cartRepository.delete(cart);
    Cart cart = getCart(slug);
    User user = cart.getUser();
    if (user != null) {
      user.setCart(null);   // break inverse side
    }
    cart.setUser(null);       // break owning side
    cartRepository.delete(cart);
    //    Cart cart = getCart(slug);
    //    cartItemRepository.deleteAllByCartSlug(slug);
    //    cart.getItems().clear();
    //        cartRepository.deleteBySlug(slug);
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
  public Cart initializeNewCart(User user){
    log.info("User data here in the user={}", user);
    return Optional.ofNullable(getCartByUserSlug(user.getSlug()))
      .orElseGet(() -> {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
      });
  }

  //  @Override
  //  public String initializeNewCart(){
  //    Cart newCart = new Cart();
  //    String newCartSlug = UUID.randomUUID().toString();
  //    newCart.setSlug(newCartSlug);
  //    return cartRepository.save(newCart).getSlug();
  //  }

  @Override
  public Cart getCartByUserSlug(String slug){
   User user = userRepository.findBySlug(slug)
       .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return cartRepository.findByUser(user);
  }


  private User getCurrentUser() {

    StoreUserDetails principal =
        (StoreUserDetails) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();

    return userRepository.findBySlug(principal.getSlug())
        .orElseThrow(() ->
            new UsernameNotFoundException("User not found"));
  }

}
