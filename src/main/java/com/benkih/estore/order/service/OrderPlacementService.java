package com.benkih.estore.order.service;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.cart.entity.CartItem;
import com.benkih.estore.cart.service.CartService;
import com.benkih.estore.checkout.dto.DiscountQuote;
import com.benkih.estore.checkout.dto.ShippingQuote;
import com.benkih.estore.checkout.dto.TaxQuote;
import com.benkih.estore.checkout.service.DiscountService;
import com.benkih.estore.checkout.service.ShippingService;
import com.benkih.estore.checkout.service.TaxService;
import com.benkih.estore.common.enums.DeliveryMethod;
import com.benkih.estore.common.enums.OrderStatus;
import com.benkih.estore.common.enums.ProductStatus;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.inventory.service.InventoryService;
import com.benkih.estore.order.dto.response.OrderResponseDto;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import com.benkih.estore.order.repository.OrderRepository;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.entity.ProductVariant;
import com.benkih.estore.user.entity.Address;
import com.benkih.estore.user.repository.AddressRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderPlacementService {
    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final DiscountService discountService;
    private final TaxService taxService;
    private final ShippingService shippingService;
    private final OrderQueryService orderQueryService;
    private final OrderPricingService orderPricingService;

    @Transactional
    public OrderResponseDto placeOrder(
            String userSlug,
            String couponCode,
            DeliveryMethod deliveryMethod,
            String shippingAddressSlug) {

        Cart cart = cartService.getCartByUserSlug(userSlug);
        System.out.println("Cart object = " + System.identityHashCode(cart));

        validateCart(cart);
        validateProducts(cart);
        Address shippingAddress =
                addressRepository
                        .findBySlugAndUserSlug(shippingAddressSlug, userSlug)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Shipping address not found"));

        cart.getItems()
                .forEach(
                        item ->
                                System.out.println(
                                        "CartItem id="
                                                + item.getId()
                                                + " hash="
                                                + System.identityHashCode(item)
                                                + " product="
                                                + item.getVariant().getProduct().getName()));
        Order order = createOrder(cart);

        List<OrderItem> items = createOrderItems(order, cart);

        order.setOrderItems(items);
        BigDecimal subtotal = orderPricingService.calculateSubtotal(items);

        DiscountQuote discountQuote = discountService.quote(cart, subtotal, couponCode, userSlug);

        //    BigDecimal discountAmount = discountQuote.getAmount();
        BigDecimal discountAmount = discountQuote.amount();

        TaxQuote taxQuote = taxService.quote(items, discountAmount, order.getShippingAddress());

        //    BigDecimal taxAmount = taxQuote.getAmount();
        BigDecimal taxAmount = taxQuote.amount();

        ShippingQuote shippingQuote = shippingService.quote(cart, shippingAddress, deliveryMethod);

        //    BigDecimal shippingAmount = shippingQuote.getAmount();
        BigDecimal shippingAmount = shippingQuote.amount();

        BigDecimal totalAmount =
                orderPricingService.calculateTotal(
                        subtotal, shippingAmount, taxAmount, discountAmount);

        order.setSubTotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setTaxAmount(taxAmount);
        order.setShippingFee(shippingAmount);
        order.setTotalAmount(totalAmount);

        //    order.setTotalAmount(orderPricingService.calculateTotalAmount(items));
        Order savedOrder = orderRepository.save(order);

        cartService.clearCart(cart.getSlug());

        Order fetched =
                orderRepository
                        .findBySlug(savedOrder.getSlug())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return orderQueryService.convertToDto(fetched);
    }

    private Order createOrder(Cart cart) {
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(Instant.now());

        return order;
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart) {
        List<OrderItem> items = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            //      inventoryService.reserve(
            //          cartItem.getProduct().getSlug(),
            //          cartItem.getQuantity()
            //      );

            Product product = cartItem.getVariant().getProduct();
            ProductVariant variant = cartItem.getVariant();

            if (product == null) {
                throw new BadRequestException("Cart item has no product");
            }

            if (variant == null) {
                throw new BadRequestException("Cart item has no product variant");
            }

            if (!variant.isActive()) {
                throw new BadRequestException(
                        "Product variant is no longer available: " + variant.getSku());
            }

            // Reserve the exact variant, not the product.
            inventoryService.reserve(variant.getSlug(), cartItem.getQuantity());

            //      BigDecimal price = variant.getPrice();
            BigDecimal price = cartItem.getUnitPrice();

            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem item =
                    new OrderItem(
                            cartItem.getQuantity(),
                            price,
                            product.getName(),
                            variant.getSku(),
                            product.getBrand(),
                            variant.getCurrency(),
                            product.getTaxCategory(),
                            order,
                            product,
                            variant,
                            product.isReturnable(),
                            product.getReturnWindowDays(),
                            product.getBusiness(),
                            subtotal);

            items.add(item);
        }

        return items;
    }

    private void validateCart(Cart cart) {

        if (cart == null) {
            throw new ResourceNotFoundException("Cart not found");
        }

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot place order with an empty cart");
        }

        for (CartItem item : cart.getItems()) {

            if (item.getQuantity() <= 0) {
                throw new BadRequestException("Cart item quantity must be greater than zero");
            }

            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Cart item has an invalid price");
            }

            if (item.getVariant().getProduct() == null) {
                throw new BadRequestException("Cart contains an invalid product");
            }
        }
    }

    private void validateProducts(Cart cart) {

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getVariant().getProduct();
            ProductVariant variant = cartItem.getVariant();

            if (product == null) {
                throw new BadRequestException("Cart contains an invalid product");
            }

            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new BadRequestException(
                        "Product is no longer available: " + product.getName());
            }

            if (product.getBusiness() == null) {
                throw new BadRequestException(
                        "Product has no associated business: " + product.getName());
            }

            if (variant.getPrice() == null || variant.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException(
                        "Product '" + product.getName() + "' has an invalid price");
            }

            if (cartItem.getQuantity() <= 0) {
                throw new BadRequestException("Invalid quantity for product: " + product.getName());
            }
        }
    }
}
