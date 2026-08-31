package com.benkih.estore.cart.service;


import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.cart.repository.CartRepository;
import com.benkih.estore.cart.entity.CartItem;
import com.benkih.estore.cart.repository.CartItemRepository;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.entity.ProductVariant;
import com.benkih.estore.product.repository.ProductVariantRepository;
import com.benkih.estore.product.service.IProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartItemService implements ICartItemService {
  private final CartItemRepository cartItemRepository;
  private final CartRepository cartRepository;
  private final IProductService productService;
  private final ICartService cartService;
  private final ProductVariantRepository productVariantRepository;


  @Override
  public void addItemToCart(String cartSlug, String variantSlug, int quantity) {
    if (quantity <= 0) {
      throw new BadRequestException(
          "Quantity must be greater than zero."
      );
    }

    Cart cart = cartService.getCart(cartSlug);

    ProductVariant variant = productVariantRepository
        .findBySlug(variantSlug)
        .orElseThrow(() ->
            new ResourceNotFoundException("Product variant not found")
        );

    if (!variant.isActive()) {
      throw new BadRequestException("This product variant is not available.");
    }

    CartItem cartItem = cart.getItems()
        .stream()
        .filter(item -> item.getVariant().getSlug().equals(variantSlug))
        .findFirst()
        .orElse(null);

    if(cartItem == null){
      cartItem = new CartItem();

      cartItem.setCart(cart);
      cartItem.setVariant(variant);
      cartItem.setQuantity(quantity);
      cartItem.setUnitPrice(variant.getPrice());
      cartItem.setTotalPrice();

      cart.addItem(cartItem);

    } else {
      cartItem.setQuantity(cartItem.getQuantity() + quantity);
      cartItem.setUnitPrice(variant.getPrice());
      cartItem.setTotalPrice();

    }
    cart.updateTotalAmount();

    cartItemRepository.save(cartItem);
    cartRepository.save(cart);
  }


  @Transactional
  @Override
  public void removeItemFromCart(String cartSlug, String productSlug) {
    Cart cart = cartService.getCart(cartSlug);
    CartItem itemToRemove = getCartItem(cartSlug, productSlug);
    cart.removeItem(itemToRemove);

    cart.updateTotalAmount();
    cartRepository.save(cart);
  }


  @Override
  public void updateItemQuantity(String cartSlug, String variantSlug,
                                 int quantity) {
    if (quantity <= 0) {
      removeItemFromCart(cartSlug, variantSlug);
      return;
    }

    Cart cart = cartService.getCart(cartSlug);
    CartItem cartItem = getCartItem(
        cartSlug,
        variantSlug
    );

    ProductVariant variant = cartItem.getVariant();

    if (!variant.isActive()) {
      throw new BadRequestException("This product variant is no longer available.");
    }

//    cart.getItems()
//        .stream().filter(item -> item.getProduct().getSlug().equals(variantSlug))
//        .findFirst()
//        .ifPresent(item -> {
//          item.setQuantity(quantity);
//          item.setUnitPrice(item.getProduct().getPrice());
//          item.setTotalPrice();
//        });

    cartItem.setQuantity(quantity);
    cartItem.setUnitPrice(variant.getPrice());
    cartItem.setTotalPrice();
    cart.updateTotalAmount();

    cartRepository.save(cart);
  }


  @Override
  public CartItem getCartItem(String cartSlug, String variantSlug){
    Cart cart = cartService.getCart(cartSlug);
    return  cart.getItems()
        .stream()
        .filter(item -> item.getVariant().getSlug().equals(variantSlug))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Product variant was not found in this cart."));
  }
}
