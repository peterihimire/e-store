package com.benkih.estore.order.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.order.dto.response.OrderResponseDto;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {
  private final IOrderService orderService;

  @PostMapping("/order/user/{userSlug}")
  public ResponseEntity<ApiResponse> createOrder(@PathVariable String userSlug){
      OrderResponseDto order = orderService.placeOrder(userSlug); // if error, this will throw error, now global error handler will process it
      log.info("Here is the order data:{}", order);
  //      OrderResponseDto dto = orderService.convertToDto(order);
      return ResponseEntity.ok(new ApiResponse("success","Order created success", order));
  }

  @GetMapping("/order/{orderSlug}")
  public ResponseEntity<ApiResponse> getOrderBySlug(@PathVariable String orderSlug){
    OrderResponseDto order = orderService.getOrderDtoBySlug(orderSlug);
  //    OrderResponseDto orderData = orderService.convertToDto(order);
    return ResponseEntity.ok(new ApiResponse("success","Order returned success", order));
  }

  //  @GetMapping("/order/{orderSlug}")
  //  public ResponseEntity<ApiResponse> getOrderBySlug(@PathVariable String orderSlug){
  //    Order order = orderService.getOrder(orderSlug);
  //    OrderResponseDto orderData = orderService.convertToDto(order);
  //    return ResponseEntity.ok(new ApiResponse("success","Order returned success", orderData));
  //  }

  @GetMapping("/order/user/{userSlug}")
  public ResponseEntity<ApiResponse> getUserOrders(@PathVariable String userSlug){
    List<Order> orders = orderService.getUserOrders(userSlug);
    List<OrderResponseDto> orderData = orderService.getConvertedOrders(orders);
    return ResponseEntity.ok(new ApiResponse("success","User orders returned success", orderData));
  }

}
