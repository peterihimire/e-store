package com.benkih.estore.payment.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.payment.dto.request.CheckoutRequest;
import com.benkih.estore.payment.dto.response.InitializePaymentResponse;
import com.benkih.estore.payment.dto.response.PaymentResponse;
import com.benkih.estore.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments", description = "Checkout initiation and payment verification")
@RestController
@RequestMapping("${api.prefix}/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<InitializePaymentResponse>> checkout(
            @RequestBody CheckoutRequest request) {
        InitializePaymentResponse response = paymentService.checkout(request);
        return ResponseEntity.ok(
                new ApiResponse<>("success", "Checkout returned successful", response));
    }

    @SecurityRequirements(value = {})
    @GetMapping("/verify/{reference}")
    public ResponseEntity<ApiResponse<PaymentResponse>> verify(@PathVariable String reference) {
        PaymentResponse response = paymentService.verify(reference);
        return ResponseEntity.ok(
                new ApiResponse<>("success", "Payment verified success", response));
    }
}
