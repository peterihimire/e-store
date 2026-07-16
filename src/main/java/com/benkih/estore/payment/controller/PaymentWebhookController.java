package com.benkih.estore.payment.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.payment.dto.webhook.PaystackWebhookEvent;
import com.benkih.estore.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/webhooks")
@RequiredArgsConstructor
public class PaymentWebhookController {
  private final PaymentService paymentService;

  @PostMapping("/paystack")
  public ResponseEntity<ApiResponse> handlePaystackWebhook(
      @RequestHeader("x-paystack-signature") String signature,
      @RequestBody String payload) {
    log.info("Here is the controller, execution got here");
    paymentService.handlePaystackWebhook(signature, payload);
    return ResponseEntity.ok(new ApiResponse("success", "Paystack webhook success", null));
  }
}
