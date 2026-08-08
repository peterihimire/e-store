package com.benkih.estore.payment.controller;

import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.payment.dto.webhook.PaystackWebhookEvent;
import com.benkih.estore.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
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
      HttpServletRequest request,
      @RequestHeader("x-paystack-signature") String signature,
      @RequestBody String payload) {
    String url = request.getRequestURL().toString();
    log.info("Here is the controller, execution got here");
    paymentService.handleWebhook(PaymentProvider.PAYSTACK,signature, payload, url);
    return ResponseEntity.ok(new ApiResponse("success", "Paystack webhook success", null));
  }
}
