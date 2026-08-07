package com.benkih.estore.order.service;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.cart.entity.CartItem;
import com.benkih.estore.cart.service.CartService;
import com.benkih.estore.common.enums.OrderStatus;
import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.common.exceptions.DuplicatePaymentException;
import com.benkih.estore.common.exceptions.PaymentException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.inventory.service.IInventoryService;
import com.benkih.estore.notification.INotificationService;
import com.benkih.estore.notification.NotificationService;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{
  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final CartService cartService;
  private final ProductService productService;
  private final IInventoryService inventoryService;
  private final INotificationService notificationService;


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


  private Order createOrder(Cart cart){
    Order order = new Order();
    order.setUser(cart.getUser());
    order.setOrderStatus(OrderStatus.PENDING);
    order.setOrderDate(LocalDateTime.now());
    return order;
  }


  private List<OrderItem> createOrderItems(Order order, Cart cart) {
    List<OrderItem> items = new ArrayList<>();

    for (CartItem cartItem : cart.getItems()) {
      inventoryService.reserve(
          cartItem.getProduct().getSlug(),
          cartItem.getQuantity()
      );

      OrderItem item = new OrderItem(
          cartItem.getQuantity(),
          cartItem.getUnitPrice(),
          order,
          cartItem.getProduct()
      );

      items.add(item);
    }

    return items;
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
  public OrderResponseDto convertToDto(Order order) {

    List<String> productSlugs = order.getOrderItems()
        .stream()
        .map(item -> item.getProduct().getSlug())
        .toList();

    List<OrderItemResponseDto> items = order.getOrderItems()
        .stream()
        .map(item -> {
          Product product = item.getProduct();
          return new OrderItemResponseDto(
              productService.convertToDto(product),
              item.getQuantity(),
              item.getPrice()
          );
        })
        .toList();

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
  public void processPaidOrder(Order order) {
    if (order.getPaymentStatus() == PaymentStatus.PAID) {
      return;
    }

    order.setPaymentStatus(PaymentStatus.PAID);
    order.setOrderStatus(OrderStatus.CONFIRMED);

    for (OrderItem item : order.getOrderItems()) {
      inventoryService.fulfillReservation(
          item.getProduct().getSlug(),
          item.getQuantity()
      );
    }
  }


  @Transactional
  public void validateOrderCanBePaid(Order order) {
    if (order.getPaymentStatus() == PaymentStatus.PAID) {
      log.info("Order validate...={}", order.getSlug());
      throw new DuplicatePaymentException(String.format("Order %s has already been paid for.", order.getSlug())
      );
    }

    if (order.getOrderStatus() == OrderStatus.PROCESSING ||
        order.getOrderStatus() == OrderStatus.SHIPPED ||
        order.getOrderStatus() == OrderStatus.DELIVERED) {
      throw new PaymentException(
          String.format("Order %s is already being processed and cannot be paid for.", order.getSlug())
      );
    }

    if (order.getOrderStatus() == OrderStatus.CANCELLED) {
      throw new PaymentException(
          String.format("Order %s has been cancelled.", order.getSlug())
      );
    }

    if (order.getOrderStatus() == OrderStatus.EXPIRED) {
      throw new PaymentException(
          String.format("Order %s has expired.", order.getSlug())
      );
    }
  }


  @Transactional
  public OrderResponseDto changeOrderStatus(String slug, OrderStatus status) {
    Order order = orderRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    switch (status) {
      case CONFIRMED -> confirmOrder(order);
      case PROCESSING -> startProcessing(order);
      case SHIPPED -> shipOrder(order);
      case DELIVERED -> deliverOrder(order);
      case RETURN_REQUESTED -> requestReturn(order);
      case RETURNED -> completeReturn(order);
      case CANCELLED -> cancelOrder(order);
      default -> throw new IllegalArgumentException("Unsupported status: " + status);
    }
    return convertToDto(order);
  }


  @Transactional
  public void cancelOrder(Order order) {
    if (order.getOrderStatus() == OrderStatus.CANCELLED) {
      return;
    }

    if (order.getOrderStatus() != OrderStatus.PENDING &&
        order.getOrderStatus() != OrderStatus.CONFIRMED) {

      throw new IllegalStateException("Order cannot be cancelled.");
    }

    for (OrderItem item : order.getOrderItems()) {
      inventoryService.release(
          item.getProduct().getSlug(),
          item.getQuantity()
      );
    }

    order.setOrderStatus(OrderStatus.CANCELLED);
    orderRepository.save(order);
    notificationService.sendOrderCancelled(order);
  }


  @Transactional
  public void confirmOrder(Order order) {
    updateOrderStatus(order, OrderStatus.CONFIRMED);
    orderRepository.save(order);
  }


  @Transactional
  public void startProcessing(Order order) {
    updateOrderStatus(order, OrderStatus.PROCESSING);
    orderRepository.save(order);
    notificationService.sendOrderProcessing(order);
  }


  @Transactional
  public void shipOrder(Order order) {
    updateOrderStatus(order, OrderStatus.SHIPPED);
    orderRepository.save(order);
    notificationService.sendOrderShipped(order);
    //    trackingService.createTrackingNumber();
    //    notificationService.sendShipmentEmail();
    //    eventPublisher.publish(...);
  }


  @Transactional
  public void deliverOrder(Order order) {
    updateOrderStatus(order, OrderStatus.DELIVERED);
    orderRepository.save(order);
    notificationService.sendOrderDelivered(order);
  }


  @Transactional
  public void requestReturn(Order order) {
    updateOrderStatus(order, OrderStatus.RETURN_REQUESTED);
    orderRepository.save(order);
  }


  @Transactional
  public void completeReturn(Order order) {
    updateOrderStatus(order, OrderStatus.RETURNED);

    for (OrderItem item : order.getOrderItems()) {
      inventoryService.addStock(
          item.getProduct().getSlug(),
          item.getQuantity()
      );
    }

    orderRepository.save(order);
  }


  private void updateOrderStatus(Order order, OrderStatus newStatus) {
    if (!isValidTransition(order.getOrderStatus(), newStatus)) {
      throw new IllegalStateException("Cannot change order status from " + order.getOrderStatus() + " to " + newStatus);
    }

    order.setOrderStatus(newStatus);
  }


  private boolean isValidTransition(OrderStatus current, OrderStatus next) {
    return switch (current) {
      case PENDING -> next == OrderStatus.CONFIRMED
          || next == OrderStatus.CANCELLED;
      case CONFIRMED -> next == OrderStatus.PROCESSING
          || next == OrderStatus.CANCELLED;
      case PROCESSING -> next == OrderStatus.SHIPPED;
      case SHIPPED -> next == OrderStatus.DELIVERED;
      case DELIVERED -> next == OrderStatus.RETURN_REQUESTED;
      case RETURN_REQUESTED -> next == OrderStatus.RETURNED;
      case RETURNED,
           CANCELLED,
           EXPIRED -> false;
    };
  }
}
//NEXT PHASE
// things to work on : order status update by admin - PROCESSING - SHIPPED - DELIVERED - with EMAILS
// Work on order cancellation, refunds, failed payments
// work on AUTH, Logout, refresh token, forgot password, reset password
// Advanced Roles Based Access Control
// Invite users and assign roles
// AUDIT LOGS
// use AI to ask questions about product in question, compare product, what it can do and more - AI powered e-commerce platform
