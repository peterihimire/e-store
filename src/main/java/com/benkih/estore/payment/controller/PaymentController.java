package com.benkih.estore.payment.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.payment.dto.request.CheckoutRequest;
import com.benkih.estore.payment.dto.response.InitializePaymentResponse;
import com.benkih.estore.payment.dto.response.PaymentResponse;
import com.benkih.estore.payment.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/payments")
@RequiredArgsConstructor
public class PaymentController {
  private final IPaymentService paymentService;

  @PostMapping("/checkout")
  public ResponseEntity<ApiResponse> checkout(@RequestBody CheckoutRequest request) {
    InitializePaymentResponse response = paymentService.checkout(request);
    return ResponseEntity.ok(new ApiResponse("success", "Checkout returned successful", response));
  }

  @GetMapping("/verify/{reference}")
  public ResponseEntity<ApiResponse> verify(@PathVariable String reference){
    PaymentResponse response = paymentService.verify(reference);
    return ResponseEntity.ok(new ApiResponse("success", "Payment verified success", response));
  }
}
