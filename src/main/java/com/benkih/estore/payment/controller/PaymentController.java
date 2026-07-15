package com.benkih.estore.payment.controller;

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
  public ResponseEntity<InitializePaymentResponse> checkout(@RequestBody CheckoutRequest request) {
    return ResponseEntity.ok(paymentService.checkout(request));
  }

  @GetMapping("/verify/{reference}")
  public ResponseEntity<PaymentResponse> verify(@PathVariable String reference){
    return ResponseEntity.ok(paymentService.verify(reference));
  }
}
