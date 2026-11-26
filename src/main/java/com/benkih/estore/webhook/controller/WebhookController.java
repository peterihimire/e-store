package com.benkih.estore.webhook.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.webhook.service.WebhookService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirements(value = {})
@Tag(name = "Webhooks", description = "Inbound payment gateway webhooks")
@RestController
@RequestMapping("${api.prefix}/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    private final WebhookService webhookService;

    @PostMapping("/paystack")
    public ResponseEntity<ApiResponse<Void>> handlePaystackWebhook(
            HttpServletRequest request,
            @RequestHeader("x-paystack-signature") String signature,
            @RequestBody String payload) {
        String url = request.getRequestURL().toString();
        log.info("Received Paystack webhook");

        webhookService.handlePaystackWebhook(signature, payload, url);

        return ResponseEntity.ok(new ApiResponse<>("success", "Paystack webhook received", null));
    }
}
