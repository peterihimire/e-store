package com.benkih.estore.vendor;

import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.payment.dto.request.InitializePaymentRequest;
import com.benkih.estore.payment.dto.request.PaystackInitializeRequest;
import com.benkih.estore.payment.dto.response.InitializePaymentResponse;
import com.benkih.estore.payment.dto.response.PaystackInitializeResponse;
import com.benkih.estore.payment.dto.response.PaystackVerifyResponse;
import com.benkih.estore.payment.dto.response.VerifyPaymentResponse;
import com.benkih.estore.payment.dto.webhook.PaystackWebhookEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.util.DigestUtils;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class PaystackClient {
  private final WebClient webClient;
  private final ObjectMapper objectMapper;

  @Value("${paystack.secret-key}")
  private String secretKey;

  @Value("${paystack.base-url}")
  private String baseUrl;


  public InitializePaymentResponse initialize(InitializePaymentRequest request){

    // with using @Data annotation
    //    PaystackInitializeRequest body = new PaystackInitializeRequest();
    //
    //    body.setEmail(request.getEmail());
    //    body.setAmount(request.getAmount().multiply(BigDecimal.valueOf(100)));
    //    body.setReference(request.getReference());
    //    body.setCurrency(request.getCurrency().name());
    //    body.setCallbackUrl(request.getCallbackUrl());

    // with using @Builder and the rest annotations
    PaystackInitializeRequest body = PaystackInitializeRequest.builder()
        .email(request.getEmail())
        .amount(request.getAmount()
            .multiply(BigDecimal.valueOf(100)))
            .reference(request.getReference())
            .currency(request.getCurrency().name())
            .callbackUrl(request.getCallbackUrl())
            .build();

    return webClient.post().uri(baseUrl+"/transaction/initialize")
        .header(HttpHeaders.AUTHORIZATION, "Bearer "+secretKey)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(PaystackInitializeResponse.class)
        .map(this::map)
        .block();
  }

  public VerifyPaymentResponse verify(String reference){
    return webClient.get().uri(baseUrl+ "/transaction/verify/"+reference)
        .header(HttpHeaders.AUTHORIZATION, "Bearer "+secretKey)
        .retrieve()
        .bodyToMono(PaystackVerifyResponse.class)
        .map(this::map)
        .block();
  }


  public boolean verifyWebhookSignature(String signature, String payload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA512");

      mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
      byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
      String computed = HexFormat.of().formatHex(hash);

      return computed.equalsIgnoreCase(signature);

    } catch (Exception e) {
      throw new RuntimeException("Unable to verify Paystack webhook signature", e);
    }
  }


  public PaystackWebhookEvent parseWebhook(String payload) {
    try {
      return objectMapper.readValue(payload, PaystackWebhookEvent.class);
    } catch (Exception e) {
      throw new RuntimeException("Unable to parse Paystack webhook payload", e);
    }
  }


  private InitializePaymentResponse map(
      PaystackInitializeResponse response) {

    return new InitializePaymentResponse(
        response.getData().getAuthorizationUrl(),
        response.getData().getAccessCode(),
        response.getData().getReference()
    );
  }

  private VerifyPaymentResponse map(PaystackVerifyResponse response) {

    BigDecimal amount = BigDecimal
        .valueOf(response.getData().getAmount())
        .movePointLeft(2);

    String authorizationCode = null;

    if (response.getData().getAuthorization() != null) {
      authorizationCode = response.getData()
          .getAuthorization()
          .getAuthorizationCode();
    }

    return new VerifyPaymentResponse(
        String.valueOf(response.getData().getId()),
        response.getData().getReference(),
        mapStatus(response.getData().getStatus()),
        amount,
        response.getData().getGatewayResponse(),
        authorizationCode,
        response.getData().getPaidAt()
    );
  }

  private PaymentStatus mapStatus(String status) {
    return switch (status.toLowerCase()) {
      case "success" -> PaymentStatus.SUCCESS;
      case "failed", "reversed" -> PaymentStatus.FAILED;
      default -> PaymentStatus.PENDING;
    };
  }
}
