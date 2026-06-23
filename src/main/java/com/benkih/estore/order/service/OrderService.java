package com.benkih.estore.order.service;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.cart.service.CartService;
import com.benkih.estore.common.enums.OrderStatus;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import com.benkih.estore.order.repository.OrderRepository;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{
  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final CartService cartService;

  @Override
  public Order placeOrder(String userSlug) {
    Cart cart = cartService.getCartByUserSlug(userSlug);
    Order order = createOrder(cart);
    List<OrderItem> orderItemList = createOrderItems(order, cart);
    order.setOrderItems(new HashSet<>(orderItemList));
    order.setTotalAmount(calculateTotalAmount(orderItemList));
    Order savedOrder = orderRepository.save(order);
    cartService.clearCart(cart.getSlug());

    return savedOrder;
  }

  private Order createOrder(Cart cart){
    Order order = new Order();
    order.setUser(cart.getUser());
    order.setOrderStatus(OrderStatus.PENDING);
    order.setOrderDate(LocalDate.now());
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

  @Override
  public Order getOrder(String slug) {
   Order order = orderRepository.findBySlug(slug)
       .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
   return order;
  }

  @Override
  public List<Order> getUserOrders(String slug){
    return orderRepository.findByUserSlug(slug);
  }
}
