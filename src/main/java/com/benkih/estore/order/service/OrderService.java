package com.benkih.estore.order.service;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.cart.service.CartService;
import com.benkih.estore.common.enums.OrderStatus;
import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.order.dto.response.OrderItemResponseDto;
import com.benkih.estore.order.dto.response.OrderResponseDto;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import com.benkih.estore.order.repository.OrderRepository;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.repository.ProductRepository;
import com.benkih.estore.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{
  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final CartService cartService;
  private final ProductService productService;

  @Override
  @Transactional
  public OrderResponseDto placeOrder(String userSlug) {
    Cart cart = cartService.getCartByUserSlug(userSlug);
    System.out.println("Cart object = " + System.identityHashCode(cart));

    cart.getItems().forEach(item ->
        System.out.println(
            "CartItem id=" + item.getId()
                + " hash=" + System.identityHashCode(item)
                + " product=" + item.getProduct().getName()
        )
    );
    Order order = createOrder(cart);
    System.out.println("Cart size = " + cart.getItems().size());
    List<OrderItem> items = createOrderItems(order, cart);
    System.out.println("Created = " + items.size());
    order.setOrderItems(items);
    System.out.println("Order before save = " + order.getOrderItems().size());
    order.setTotalAmount(calculateTotalAmount(items));
    Order saved = orderRepository.save(order);
    System.out.println("Saved order = " + saved.getOrderItems().size());
    cartService.clearCart(cart.getSlug());
    Order fetched = orderRepository.findBySlug(saved.getSlug())
        .orElseThrow();
    System.out.println("Fetched order = " + fetched.getOrderItems().size());
    return convertToDto(fetched);
  }
  //  public Order placeOrder(String userSlug) {
  //    Cart cart = cartService.getCartByUserSlug(userSlug);
  //    Order order = createOrder(cart);
  //    List<OrderItem> orderItemList = createOrderItems(order, cart);
  ////    order.setOrderItems(new HashSet<>(orderItemList));
  //    order.setItems(orderItemList);
  //    order.setTotalAmount(calculateTotalAmount(orderItemList));
  //    Order savedOrder = orderRepository.save(order);
  //    cartService.clearCart(cart.getSlug());
  //
  //    return savedOrder;
  //  }

  private Order createOrder(Cart cart){
    Order order = new Order();
    order.setUser(cart.getUser());
    order.setOrderStatus(OrderStatus.PENDING);
    order.setOrderDate(LocalDateTime.now());
    return order;
  }

  private List<OrderItem> createOrderItems(Order order, Cart cart){
    return cart.getItems().stream().map(cartItem -> {
      Product product = cartItem.getProduct();
      product.setInventory(product.getInventory() - cartItem.getQuantity());
      productRepository.save(product);

      return new OrderItem(
        cartItem.getQuantity(),
        cartItem.getUnitPrice(),
        order,
        product);
    }).toList();
  }

  private BigDecimal calculateTotalAmount(List<OrderItem> orderItemList){
    return orderItemList.stream()
        .map(item -> item.getPrice()
        .multiply(new BigDecimal(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Transactional
  @Override
  public Order getOrder(String slug) {
   Order order = orderRepository.findBySlug(slug)
       .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
   return order;
  }

  @Transactional
  @Override
  public OrderResponseDto getOrderDtoBySlug(String slug) {
    Order order = orderRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    return convertToDto(order);
  }

  @Override
  public List<Order> getUserOrders(String slug){
    return orderRepository.findByUserSlug(slug);
  }


  @Transactional(readOnly = true)
  @Override
  public List<OrderResponseDto> getConvertedOrders(List<Order> orders) {
    return orders.stream().map(this::convertToDto).toList();
  }

  @Transactional(readOnly = true)
  @Override
  public OrderResponseDto convertToDto(Order order){
    List<OrderItemResponseDto> items = order.getOrderItems()
        .stream()
        .map(item -> new OrderItemResponseDto( // make sure to follow the arrangement from the DTO
            productService.convertToDto(item.getProduct()),
            item.getQuantity(),
            item.getPrice()
        )).toList();
    return new OrderResponseDto(
        order.getSlug(),
        order.getUser().getSlug(),
        order.getOrderDate(),
        order.getTotalAmount(),
        order.getOrderStatus().name(),
        items
    );
  }

  @Transactional
  public void markAsPaid(Order order) {
    log.info("Before: {}", order.getPaymentStatus());
    if (order.getPaymentStatus() != PaymentStatus.PAID) {
      order.setPaymentStatus(PaymentStatus.PAID);
    }
    log.info("After: {}", order.getPaymentStatus());
  }
}
