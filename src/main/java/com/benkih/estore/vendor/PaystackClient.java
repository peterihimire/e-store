package com.benkih.estore.vendor;

import com.benkih.estore.audit.entity.ApiLog;
import com.benkih.estore.audit.service.ApiLogService;
import com.benkih.estore.audit.service.IApiLogService;
import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.common.enums.RefundStatus;
import com.benkih.estore.common.exceptions.PaymentGatewayException;
import com.benkih.estore.payment.dto.request.InitializePaymentRequest;
import com.benkih.estore.payment.dto.request.PaystackInitializeRequest;
import com.benkih.estore.payment.dto.request.RefundPaymentRequest;
import com.benkih.estore.payment.dto.response.*;
import com.benkih.estore.payment.dto.webhook.PaystackWebhookEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class PaystackClient {
  private final WebClient webClient;
  private final ObjectMapper objectMapper;
  private final IApiLogService apiLogService;

  @Value("${paystack.secret-key}")
  private String secretKey;

  @Value("${paystack.base-url}")
  private String baseUrl;


  public InitializePaymentResponse initialize(InitializePaymentRequest request){

    PaystackInitializeRequest body = PaystackInitializeRequest.builder()
        .email(request.getEmail())
        .amount(request.getAmount().multiply(BigDecimal.valueOf(100)))
        .reference(request.getReference())
        .currency(request.getCurrency().name())
        .callbackUrl(request.getCallbackUrl())
        .build();

    try {
      PaystackInitializeResponse response = webClient.post()
          .uri(baseUrl + "/transaction/initialize")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
          .bodyValue(body)
          .retrieve()
          .bodyToMono(PaystackInitializeResponse.class)
          .block();

      apiLogService.saveOutboundLog(
          "POST",
          baseUrl + "/transaction/initialize",
          body,
          200,
          response,
          null);

      return mapInitializeResponse(response);
    } catch (Exception e){
      apiLogService.saveOutboundLog(
          "POST",
          baseUrl + "/transaction/initialize",
          body,
          500,
          e.getMessage(),
          e
      );
        throw e;
    }
  }


  public VerifyPaymentResponse verify(String reference){
    try {
      PaystackVerifyResponse response = webClient.get()
          .uri(baseUrl + "/transaction/verify/" + reference)
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
          .retrieve()
          .bodyToMono(PaystackVerifyResponse.class)
          .block();

      apiLogService.saveOutboundLog(
          "GET",
          baseUrl + "/transaction/verify/" + reference,
          null,
          200,
          response,
          null);

      return mapVerifyResponse(response);
    } catch(Exception e){
      apiLogService.saveOutboundLog(
          "GET",
          baseUrl + "/transaction/verify/" + reference,
          null,
          500,
          e.getMessage(),
          e
      );
      throw e;
    }
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

  // work this
  public RefundPaymentResponse refund(RefundPaymentRequest request) {

    try {

      PaystackRefundResponse response = webClient.post()
          .uri(baseUrl + "/refund")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
          .bodyValue(request)
          .retrieve()
          .bodyToMono(PaystackRefundResponse.class)
          .block();

      apiLogService.saveOutboundLog(
          "POST",
          baseUrl + "/refund",
          request,
          200,
          response,
          null
      );

      return mapRefundResponse(response);

    } catch (Exception e) {

      apiLogService.saveOutboundLog(
          "POST",
          baseUrl + "/refund",
          request,
          500,
          e.getMessage(),
          e
      );

      throw new PaymentGatewayException("Unable to initiate refund", e);
    }
  }

  // work that
  public RefundPaymentResponse verifyRefund(String reference) {

    try {

      PaystackRefundResponse response = webClient.get()
          .uri(baseUrl + "/refund/" + reference)
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
          .retrieve()
          .bodyToMono(PaystackRefundResponse.class)
          .block();

      apiLogService.saveOutboundLog(
          "GET",
          baseUrl + "/refund/" + reference,
          null,
          200,
          response,
          null
      );

      return mapRefundResponse(response);

    } catch (Exception e) {

      apiLogService.saveOutboundLog(
          "GET",
          baseUrl + "/refund/" + reference,
          null,
          500,
          e.getMessage(),
          e
      );

      throw new PaymentGatewayException("Unable to verify refund", e);
    }
  }


  private InitializePaymentResponse mapInitializeResponse(PaystackInitializeResponse response) {
    return new InitializePaymentResponse(
        response.isStatus(),
        response.getData().getAuthorizationUrl(),
        response.getData().getAccessCode(),
        response.getData().getReference(),
        response.getMessage()
    );
  }


  private VerifyPaymentResponse mapVerifyResponse(PaystackVerifyResponse response) {
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

  private RefundPaymentResponse mapRefundResponse(PaystackRefundResponse response) {

    RefundPaymentResponse dto = new RefundPaymentResponse();

    dto.setSuccess(response.isStatus());
    dto.setMessage(response.getMessage());
    dto.setRefundReference(response.getData().getRefundReference());
    dto.setTransactionReference(response.getData().getTransactionReference());
    dto.setStatus(mapRefundStatus(response.getData().getStatus()));
    dto.setAmount(
        BigDecimal.valueOf(response.getData().getAmount()).movePointLeft(2)
    );
    dto.setCurrency(response.getData().getCurrency());
    dto.setReason(response.getData().getReason());
    dto.setCreatedAt(response.getData().getCreatedAt());

    return dto;
  }

  private RefundStatus mapRefundStatus(String status) {

    if (status == null) {
      return RefundStatus.PENDING;
    }

    return switch (status.toLowerCase()) {
      case "pending" -> RefundStatus.PENDING;
      case "processing" -> RefundStatus.PROCESSING;
      case "processed", "success", "completed" -> RefundStatus.SUCCESS;
      case "failed" -> RefundStatus.FAILED;
      case "needs-attention" -> RefundStatus.NEEDS_ATTENTION;
      default -> {
        log.warn("Unknown Paystack refund status: {}", status);
        yield RefundStatus.UNKNOWN;
      }
    };
  }
}
