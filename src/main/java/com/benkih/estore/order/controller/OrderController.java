package com.benkih.estore.order.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.order.dto.request.UpdateOrderStatusRequest;
import com.benkih.estore.order.dto.response.OrderResponseDto;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.service.IOrderService;
import com.benkih.estore.security.user.StoreUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {
  private final IOrderService orderService;

  @PostMapping("/order/place-order")
  public ResponseEntity<ApiResponse> createOrder(@AuthenticationPrincipal StoreUserDetails userDetails){
      OrderResponseDto order = orderService.placeOrder(userDetails.getSlug()); // if error, this will throw error, now global error handler will process it
      log.info("Here is the order data:{}", order);
  //      OrderResponseDto dto = orderService.convertToDto(order);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(new ApiResponse("success","Order created success", order));
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

  @GetMapping("/order/user")
  public ResponseEntity<ApiResponse> getUserOrders(){
    List<Order> orders = orderService.getUserOrders();
    List<OrderResponseDto> orderData = orderService.getConvertedOrders(orders);
    return ResponseEntity.ok(new ApiResponse(
        "success",
        "User orders returned success",
        orderData));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{slug}/status")
  public ResponseEntity<ApiResponse> changeStatus(
      @PathVariable String slug,
      @RequestBody UpdateOrderStatusRequest request) {

    OrderResponseDto order = orderService.changeOrderStatus(slug, request.getStatus());

    return ResponseEntity.ok(
        new ApiResponse("success", "Order updated successfully", order)
    );
  }
}
