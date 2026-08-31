package com.benkih.estore.order.service;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.business.repository.BusinessRepository;
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
import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.common.enums.ProductStatus;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.common.exceptions.DuplicatePaymentException;
import com.benkih.estore.common.exceptions.PaymentException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.inventory.service.IInventoryService;
import com.benkih.estore.notification.INotificationService;
import com.benkih.estore.notification.NotificationService;
import com.benkih.estore.order.dto.response.BusinessOrderItemResponseDto;
import com.benkih.estore.order.dto.response.BusinessOrderResponseDto;
import com.benkih.estore.order.dto.response.OrderItemResponseDto;
import com.benkih.estore.order.dto.response.OrderResponseDto;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import com.benkih.estore.order.repository.OrderItemRepository;
import com.benkih.estore.order.repository.OrderRepository;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.entity.ProductVariant;
import com.benkih.estore.product.repository.ProductRepository;
import com.benkih.estore.product.service.ProductService;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
  private final CurrentUserService currentUserService;
  private final BusinessRepository businessRepository;
  private final OrderItemRepository orderItemRepository;
  private final TaxService taxService;
  private final DiscountService discountService;
  private final ShippingService shippingService;



  @Override
  @Transactional
  public OrderResponseDto placeOrder(
      String userSlug,
      String couponCode,
      DeliveryMethod deliveryMethod) {

    Cart cart = cartService.getCartByUserSlug(userSlug);
    System.out.println("Cart object = " + System.identityHashCode(cart));

    validateCart(cart);
    validateProducts(cart);

    cart.getItems().forEach(item ->
        System.out.println(
            "CartItem id=" + item.getId()
                + " hash=" + System.identityHashCode(item)
                + " product=" + item.getVariant().getProduct().getName()
        )
    );
    Order order = createOrder(cart);

    List<OrderItem> items = createOrderItems(order, cart);

    order.setOrderItems(items);
    BigDecimal subtotal = calculateSubtotal(items);

    DiscountQuote discountQuote = discountService.quote(
        cart,
        subtotal,
        couponCode,
        userSlug
    );

    BigDecimal discountAmount = discountQuote.getAmount();

    TaxQuote taxQuote = taxService.quote(
        items,
        discountAmount,
        order.getShippingAddress()
    );

    BigDecimal taxAmount = taxQuote.getAmount();

    ShippingQuote shippingQuote = shippingService.quote(
        cart,
        order.getShippingAddress(),
        deliveryMethod
    );

    BigDecimal shippingAmount = shippingQuote.getAmount();

    BigDecimal totalAmount = calculateTotal(
        subtotal,
        shippingAmount,
        taxAmount,
        discountAmount
    );

//    BigDecimal discountAmount = discountService.calculateDiscount(cart, subtotal);
//
//    BigDecimal taxableAmount = subtotal.subtract(discountAmount);
//
//    BigDecimal taxAmount = taxService.calculate(taxableAmount);
//
//    BigDecimal shippingFee = shippingService.calculateShipping(
//        cart,
//        order.getShippingAddress()
//    );

    order.setSubTotal(subtotal);
    order.setDiscountAmount(discountAmount);
    order.setTaxAmount(taxAmount);
    order.setShippingFee(shippingAmount);
    order.setTotalAmount(totalAmount);

//    order.setTotalAmount(calculateTotalAmount(items));
    Order savedOrder = orderRepository.save(order);

    cartService.clearCart(cart.getSlug());

    Order fetched = orderRepository.findBySlug(savedOrder.getSlug())
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

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
//      inventoryService.reserve(
//          cartItem.getProduct().getSlug(),
//          cartItem.getQuantity()
//      );

      Product product = cartItem.getVariant().getProduct();
      ProductVariant variant = cartItem.getVariant();

      if (product == null) {
        throw new BadRequestException(
            "Cart item has no product"
        );
      }

      if (variant == null) {
        throw new BadRequestException(
            "Cart item has no product variant"
        );
      }

      if (!variant.isActive()) {
        throw new BadRequestException(
            "Product variant is no longer available: "
                + variant.getSku()
        );
      }

      // Reserve the exact variant, not the product.
      inventoryService.reserve(
          variant.getSlug(),
          cartItem.getQuantity()
      );

      BigDecimal price = variant.getPrice();

      OrderItem item = new OrderItem(
          cartItem.getQuantity(),
          price,
          product.getName(),
          variant.getSku(),
          product.getBrand(),
          variant.getCurrency(),
          product.getTaxCategory(),
          order,
          product,
          product.getBusiness()
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


  @Transactional(readOnly = true)
  @Override
  public List<Order> getUserOrders(){
    User user = currentUserService.getCurrentUser();
//    return orderRepository.findByUserSlug(user.getSlug());
    List<Order> orders = orderRepository.findByUserSlug(user.getSlug());

    orders.forEach(order ->
        order.getOrderItems().forEach(item ->
            item.getProduct().getImages().size()
        )
    );

    return orders;
  }


  public BusinessOrderResponseDto getBusinessOrder(String slug,
                                                   Long businessId){

    Business business = businessRepository.findById(businessId)
        .orElseThrow(() ->
            new ResourceNotFoundException("Business not found"));

    List<OrderItem> businessItems = orderItemRepository.findBusinessOrderItemsByOrderSlug(slug, business.getSlug());

    if (businessItems.isEmpty()) {
      throw new ResourceNotFoundException("Order not found");
    }

    Order order = businessItems.get( 0).getOrder();

    return convertBusinessOrderToDto(
        order,
        businessItems
    );
  }

  public List<BusinessOrderResponseDto> getBusinessOrders(Long businessId){
    Business business = businessRepository.findById(businessId)
        .orElseThrow(() ->
            new ResourceNotFoundException("Business not found"));

    List<OrderItem> orderItems = orderItemRepository.findBusinessOrderItems(
            business.getSlug()
        );

    Map<Order, List<OrderItem>> groupedOrders = orderItems
        .stream()
        .collect(Collectors.groupingBy(
            OrderItem::getOrder,
            LinkedHashMap::new,
            Collectors.toList()
        ));

    return groupedOrders.entrySet()
        .stream()
        .map(entry ->
            convertBusinessOrderToDto(
                entry.getKey(),
                entry.getValue()
            )
        )
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public BusinessOrderItemResponseDto getBusinessOrderItem(String slug,
                                                           Long businessId) {

    Business business = businessRepository.findById(businessId)
        .orElseThrow(() ->
            new ResourceNotFoundException("Business not found"));

    OrderItem orderItem = orderItemRepository.findBusinessOrderItem(
            slug,
            business.getSlug()
        )
        .orElseThrow(() ->
            new ResourceNotFoundException("Order item not found")
        );

    return convertBusinessOrderItemToDto(orderItem);
  }


  @Transactional(readOnly = true)
  @Override
  public List<OrderResponseDto> getConvertedOrders(List<Order> orders) {
    return orders.stream()
        .map(this::convertToDto)
        .toList();
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
              item.getSlug(),
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

  private BusinessOrderItemResponseDto convertBusinessOrderItemToDto(
      OrderItem item
  ) {
    Order order = item.getOrder();

    BigDecimal total = item.getPrice()
        .multiply(BigDecimal.valueOf(item.getQuantity()));

    return new BusinessOrderItemResponseDto(
        item.getSlug(),

        order.getSlug(),
        order.getOrderNumber(),

        item.getProduct().getSlug(),
        item.getName(),
        item.getSku(),
        item.getBrand(),

        item.getQuantity(),
        item.getPrice(),
        total,

        order.getOrderStatus().name(),
        order.getPaymentStatus().name(),

        order.getOrderDate()
    );
  }

  private BusinessOrderResponseDto convertBusinessOrderToDto(
      Order order,
      List<OrderItem> businessItems
  ) {
    List<BusinessOrderItemResponseDto> items = businessItems
        .stream()
        .map(this::convertBusinessOrderItemToDto)
        .toList();

    BigDecimal subtotal = businessItems
        .stream()
        .map(item ->
            item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
        )
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new BusinessOrderResponseDto(
        order.getSlug(),
        order.getOrderNumber(),
        order.getOrderDate(),
        order.getOrderStatus().name(),
        order.getPaymentStatus().name(),
        items,
        subtotal,
        subtotal
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
          || next == OrderStatus.CANCELLED
          || next == OrderStatus.EXPIRED;
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

  private void validateCart(Cart cart) {

    if (cart == null) {
      throw new ResourceNotFoundException("Cart not found");
    }

    if (cart.getItems() == null || cart.getItems().isEmpty()) {
      throw new BadRequestException(
          "Cannot place order with an empty cart"
      );
    }

    for (CartItem item : cart.getItems()) {

      if (item.getQuantity() <= 0) {
        throw new BadRequestException(
            "Cart item quantity must be greater than zero"
        );
      }

      if (item.getUnitPrice() == null ||
          item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
        throw new BadRequestException(
            "Cart item has an invalid price"
        );
      }

      if (item.getVariant().getProduct() == null) {
        throw new BadRequestException(
            "Cart contains an invalid product"
        );
      }
    }
  }

  private void validateProducts(Cart cart) {

    for (CartItem cartItem : cart.getItems()) {

      Product product = cartItem.getVariant().getProduct();
      ProductVariant  variant = cartItem.getVariant();

      if (product == null) {
        throw new BadRequestException(
            "Cart contains an invalid product"
        );
      }

      if (product.getStatus() != ProductStatus.ACTIVE) {
        throw new BadRequestException(
            "Product is no longer available: "
                + product.getName()
        );
      }

      if (product.getBusiness() == null) {
        throw new BadRequestException(
            "Product has no associated business: "
                + product.getName()
        );
      }

      if (variant.getPrice() == null ||
          variant.getPrice().compareTo(BigDecimal.ZERO) < 0) {
        throw new BadRequestException(
            "Product '" + product.getName() +
                "' has an invalid price"
        );
      }

      if (cartItem.getQuantity() <= 0) {
        throw new BadRequestException(
            "Invalid quantity for product: "
                + product.getName()
        );
      }
    }
  }

  private BigDecimal calculateSubtotal(
      List<OrderItem> items
  ) {
    return items.stream()
        .map(item ->
            item.getPrice()
                .multiply(
                    BigDecimal.valueOf(item.getQuantity())
                )
        )
        .reduce(
            BigDecimal.ZERO,
            BigDecimal::add
        )
        .setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal calculateTotal(
      BigDecimal subtotal,
      BigDecimal shippingFee,
      BigDecimal taxAmount,
      BigDecimal discountAmount
  ) {
    return subtotal
        .subtract(discountAmount)
        .add(taxAmount)
        .add(shippingFee)
        .setScale(2, RoundingMode.HALF_UP);
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
