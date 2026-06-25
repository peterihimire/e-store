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

  @PostMapping("/order")
  public ResponseEntity<ApiResponse> createOrder(@RequestParam String slug){
      Order order = orderService.placeOrder(slug); // if error, this will throw error, now global error handler will process it
      log.info("Here is the order data:{}", order);
      return ResponseEntity.ok(new ApiResponse("success","Order created success", order));
  }

  @GetMapping("/order/{slug}")
  public ResponseEntity<ApiResponse> getOrderBySlug(@PathVariable String slug){
    Order order = orderService.getOrder(slug);
    OrderResponseDto orderData = orderService.convertToDto(order);
    return ResponseEntity.ok(new ApiResponse("success","Order returned success", orderData));
  }

  @GetMapping("/order/user/{slug}")
  public ResponseEntity<ApiResponse> getUserOrders(@PathVariable String slug){
    List<Order> orders = orderService.getUserOrders(slug);
    List<OrderResponseDto> orderData = orderService.getConvertedOrders(orders);
    return ResponseEntity.ok(new ApiResponse("success","User orders returned success", orderData));
  }

}
