package com.benkih.estore.order.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.order.dto.request.PlaceOrderRequest;
import com.benkih.estore.order.dto.request.UpdateOrderStatusRequest;
import com.benkih.estore.order.dto.response.BusinessOrderItemResponseDto;
import com.benkih.estore.order.dto.response.BusinessOrderResponseDto;
import com.benkih.estore.order.dto.response.OrderResponseDto;
import com.benkih.estore.order.service.OrderService;
import com.benkih.estore.security.tenant.TenantContext;
import com.benkih.estore.security.user.StoreUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Orders", description = "Placing and tracking buyer and seller orders")
@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final TenantContext tenantContext;

    @PostMapping("/order/place-order")
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
            @AuthenticationPrincipal StoreUserDetails userDetails,
            @RequestBody PlaceOrderRequest request) {
        OrderResponseDto order =
                orderService.placeOrder(
                        userDetails.getSlug(),
                        request.getCouponCode(),
                        request.getDeliveryMethod(),
                        request.getShippingAddressSlug()); // if error, this will throw error, now
        // global error handler will process it
        log.info("Here is the order data:{}", order);
        //      OrderResponseDto dto = orderService.convertToDto(order);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("success", "Order created success", order));
    }

    @GetMapping("/order/{orderSlug}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderBySlug(
            @PathVariable String orderSlug) {
        OrderResponseDto order = orderService.getOrderDtoBySlug(orderSlug);
        //    OrderResponseDto orderData = orderService.convertToDto(order);
        return ResponseEntity.ok(new ApiResponse<>("success", "Order returned success", order));
    }

    @GetMapping("/order/user")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getUserOrders() {
        List<OrderResponseDto> orderData = orderService.getUserOrders();
        //    List<OrderResponseDto> orderData = orderService.getConvertedOrders(orders);
        return ResponseEntity.ok(
                new ApiResponse<>("success", "User orders returned success", orderData));
    }

    @PreAuthorize("hasAuthority('ORDER_UPDATE')")
    @PatchMapping("/{slug}/status")
    public ResponseEntity<ApiResponse<OrderResponseDto>> changeStatus(
            @PathVariable String slug, @RequestBody UpdateOrderStatusRequest request) {

        OrderResponseDto order = orderService.changeOrderStatus(slug, request.getStatus());

        return ResponseEntity.ok(new ApiResponse<>("success", "Order updated successfully", order));
    }

    @GetMapping("/business/get-order/{orderSlug}")
    public ResponseEntity<ApiResponse<BusinessOrderResponseDto>> getBusinessOrder(
            @PathVariable String orderSlug) {
        Long businessId = tenantContext.getBusinessId();

        BusinessOrderResponseDto orderData = orderService.getBusinessOrder(orderSlug, businessId);
        return ResponseEntity.ok(
                new ApiResponse<>("success", "Business Order returned success", orderData));
    }

    @GetMapping("/business/get-orders")
    public ResponseEntity<ApiResponse<List<BusinessOrderResponseDto>>> getBusinessOrders() {
        Long businessId = tenantContext.getBusinessId();

        List<BusinessOrderResponseDto> orderData = orderService.getBusinessOrders(businessId);
        return ResponseEntity.ok(
                new ApiResponse<>("success", "Business Orders returned success", orderData));
    }

    @GetMapping("/business/get-order-item/{orderSlug}")
    public ResponseEntity<ApiResponse<BusinessOrderItemResponseDto>> getBusinessOrderItems(
            @PathVariable String orderSlug) {
        Long businessId = tenantContext.getBusinessId();

        BusinessOrderItemResponseDto orderData =
                orderService.getBusinessOrderItem(orderSlug, businessId);
        return ResponseEntity.ok(
                new ApiResponse<>("success", "Business Order item returned success", orderData));
    }
}
