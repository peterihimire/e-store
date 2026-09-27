package com.benkih.estore.vendor;

import com.benkih.estore.audit.entity.ApiLog;
import com.benkih.estore.audit.service.ApiLogService;
import com.benkih.estore.audit.service.IApiLogService;
import com.benkih.estore.business.entity.BankAccount;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.common.enums.RefundGatewayStatus;
import com.benkih.estore.common.enums.RefundStatus;
import com.benkih.estore.common.exceptions.PaymentGatewayException;
import com.benkih.estore.common.exceptions.PayoutGatewayException;
import com.benkih.estore.payment.dto.request.InitializePaymentRequest;
import com.benkih.estore.payment.dto.request.PaystackInitializeRequest;
import com.benkih.estore.payment.dto.request.RefundPaymentRequest;
import com.benkih.estore.payment.dto.response.*;
import com.benkih.estore.payment.dto.webhook.PaystackWebhookEvent;
import com.benkih.estore.payout.dto.request.PaystackTransferRecipientRequest;
import com.benkih.estore.payout.dto.request.PaystackTransferRequest;
import com.benkih.estore.payout.dto.response.*;
import com.benkih.estore.payout.entity.Payout;
import com.benkih.estore.payout.entity.PayoutRecipient;
import com.benkih.estore.payout.enums.PayoutGatewayStatus;
import com.benkih.estore.payout.enums.PayoutTransferStatus;
import com.benkih.estore.payout.webhook.PayoutWebhookEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    log.info("Refund request peter: transactionReference={}, amount={}, reason={}",
        request.getTransactionReference(),
        request.getAmount(),
        request.getReason()
    );
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


  public PayoutRecipientResult createTransferRecipient(
      BankAccount bankAccount
  ) {

    PaystackTransferRecipientRequest body =
        PaystackTransferRecipientRequest.builder()
            .type("nuban")
            .name(bankAccount.getAccountName())
            .accountNumber(bankAccount.getAccountNumber())
            .bankCode(bankAccount.getBankCode())
            .currency(CurrencyCode.NGN.name())
            .build();

    String url = baseUrl + "/transferrecipient";

    try {

      PaystackTransferRecipientResponse response =
          webClient.post()
              .uri(url)
              .header(
                  HttpHeaders.AUTHORIZATION,
                  "Bearer " + secretKey
              )
              .header(
                  HttpHeaders.CONTENT_TYPE,
                  MediaType.APPLICATION_JSON_VALUE
              )
              .bodyValue(body)
              .retrieve()
              .bodyToMono(PaystackTransferRecipientResponse.class)
              .block();

      apiLogService.saveOutboundLog(
          "POST",
          url,
          body,
          200,
          response,
          null
      );

      if (response == null || !response.isStatus()) {
        throw new PayoutGatewayException(
            response != null
                ? response.getMessage()
                : "Unable to create Paystack transfer recipient"
        );
      }
      return mapTransferRecipientResponse(response);

//      return new PayoutRecipientResult(
//          true,
//          response.getMessage(),
//          response.getData().getRecipientCode(),
//          response.getData().getName()
//      );

    } catch (Exception e) {

      apiLogService.saveOutboundLog(
          "POST",
          url,
          body,
          500,
          e.getMessage(),
          e
      );

      if (e instanceof PayoutGatewayException) {
        throw e;
      }

      throw new PayoutGatewayException("Unable to create Paystack transfer recipient", e);
    }
  }


  public PayoutResult initiateTransfer(
      Payout payout,
      PayoutRecipient recipient
  ) {

    long amountInKobo = payout.getAmount()
            .movePointRight(2)
            .longValueExact();

    String reference = "payout-" + payout.getSlug();

    PaystackTransferRequest body = PaystackTransferRequest.builder()
            .source("balance")
            .amount(amountInKobo)
            .recipient(recipient.getRecipientCode())
            .reference(reference)
            .reason("Seller payout")
            .currency(payout.getCurrency().name())
            .build();

    String url = baseUrl + "/transfer";

    try {

      PaystackTransferResponse response = webClient.post()
              .uri(url)
              .header(
                  HttpHeaders.AUTHORIZATION,
                  "Bearer " + secretKey
              )
              .header(
                  HttpHeaders.CONTENT_TYPE,
                  MediaType.APPLICATION_JSON_VALUE
              )
              .bodyValue(body)
              .retrieve()
              .bodyToMono(PaystackTransferResponse.class)
              .block();

      apiLogService.saveOutboundLog(
          "POST",
          url,
          body,
          200,
          response,
          null
      );
      return mapTransferResponse(response);

//      if (response == null || !response.isStatus()) {
//        return PayoutResult.builder()
//            .accepted(false)
//            .status(PayoutGatewayStatus.FAILED)
//            .failureReason(
//                response != null
//                    ? response.getMessage()
//                    : "Paystack rejected the transfer"
//            )
//            .build();
//      }
//
//      return PayoutResult.builder()
//          .accepted(true)
//          .status(mapPayoutGatewayStatus(response.getData().getStatus()))
//          .providerReference(response.getData().getReference())
//          .providerTransferCode(response.getData().getTransferCode())
//          .build();

    } catch (Exception e) {

      apiLogService.saveOutboundLog(
          "POST",
          url,
          body,
          500,
          e.getMessage(),
          e
      );

      throw new PayoutGatewayException("Unable to initiate Paystack transfer", e);
    }
  }


  public PayoutVerificationResult verifyTransfer(
      String providerReference
  ) {

    String url = baseUrl + "/transfer/verify/" + providerReference;

    try {

      PaystackTransferResponse response =
          webClient.get()
              .uri(url)
              .header(
                  HttpHeaders.AUTHORIZATION,
                  "Bearer " + secretKey
              )
              .retrieve()
              .bodyToMono(PaystackTransferResponse.class)
              .block();

      apiLogService.saveOutboundLog(
          "GET",
          url,
          null,
          200,
          response,
          null
      );

//      if (response == null || !response.isStatus()) {
//        return PayoutTransferStatus.UNKNOWN;
//      }

      return mapVerifyTransferResponse(response);

    } catch (Exception e) {

      apiLogService.saveOutboundLog(
          "GET",
          url,
          null,
          500,
          e.getMessage(),
          e
      );

      throw new PayoutGatewayException(
          "Unable to verify Paystack transfer",
          e
      );
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

    BigDecimal fees = BigDecimal
        .valueOf(response.getData().getFees())
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
        response.getData().getPaidAt(),
        fees
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

  private RefundGatewayStatus mapRefundStatus(String status) {

    if (status == null) {
      return RefundGatewayStatus.PENDING;
    }

    return switch (status.toLowerCase()) {
      case "pending" -> RefundGatewayStatus.PENDING;
      case "processing" -> RefundGatewayStatus.PROCESSING;
      case "processed", "success", "completed" -> RefundGatewayStatus.SUCCESS;
      case "failed" -> RefundGatewayStatus.FAILED;
      case "needs-attention" -> RefundGatewayStatus.NEEDS_ATTENTION;
      default -> {
        log.warn("Unknown Paystack refund status: {}", status);
        yield RefundGatewayStatus.UNKNOWN;
      }
    };
  }


  private PayoutGatewayStatus mapTransferStatus(String status) {
    if (status == null) {
      return PayoutGatewayStatus.UNKNOWN;
    }

    return switch (status.toLowerCase()) {
      case "pending", "received" ->
          PayoutGatewayStatus.PENDING;

      case "success" ->
          PayoutGatewayStatus.SUCCESS;

      case "failed", "abandoned", "blocked", "rejected" ->
          PayoutGatewayStatus.FAILED;

      case "reversed" ->
          PayoutGatewayStatus.REVERSED;

      default ->
          PayoutGatewayStatus.UNKNOWN;
    };
  }

  private PayoutGatewayStatus mapInitiationStatus(String status) {
    if (status == null) {
      return PayoutGatewayStatus.UNKNOWN;
    }

    return switch (status.toLowerCase()) {
      case "pending", "received", "success" ->
          PayoutGatewayStatus.PENDING;

      case "failed", "abandoned", "blocked", "rejected" ->
          PayoutGatewayStatus.FAILED;

      case "reversed" ->
          PayoutGatewayStatus.REVERSED;

      default ->
          PayoutGatewayStatus.UNKNOWN;
    };
  }

//  private PayoutRecipientResult mapTransferRecipientResponse(
//      PaystackTransferRecipientResponse response
//  ) {
//
//    if (response == null || !response.isStatus()) {
//      throw new PayoutGatewayException(
//          response != null
//              ? response.getMessage()
//              : "Unable to create Paystack transfer recipient"
//      );
//    }
//
//    return PayoutRecipientResult.builder()
//        .success(true)
//        .message(response.getMessage())
//        .recipientCode(response.getData().getRecipientCode())
//        .recipientName(response.getData().getName())
//        .build();
//  }

  private PayoutRecipientResult mapTransferRecipientResponse(
      PaystackTransferRecipientResponse response
  ) {

    return new PayoutRecipientResult(
        response.isStatus(),
        response.getMessage(),
        response.getData().getRecipientCode(),
        response.getData().getName()
    );
  }

  private PayoutResult mapTransferResponse(
      PaystackTransferResponse response
  ) {

    return new PayoutResult(
        response.isStatus(),
        mapPayoutStatus(response.getData().getStatus()),
        response.getData().getReference(),
        response.getData().getTransferCode(),
        response.getMessage()
    );
  }

  private PayoutGatewayStatus mapPayoutStatus(String status) {

    if (status == null) {
      return PayoutGatewayStatus.PENDING;
    }

    return switch (status.toLowerCase()) {

      case "pending", "received" ->
          PayoutGatewayStatus.PENDING;

      case "success" ->
          PayoutGatewayStatus.SUCCESS;

      case "failed", "abandoned", "blocked", "rejected" ->
          PayoutGatewayStatus.FAILED;

      case "reversed" ->
          PayoutGatewayStatus.REVERSED;

      default -> {
        log.warn("Unknown Paystack transfer status: {}", status);
        yield PayoutGatewayStatus.UNKNOWN;
      }
    };
  }

  private PayoutVerificationResult mapVerifyTransferResponse(
      PaystackTransferResponse response
  ) {

    if (response == null || response.getData() == null) {

      return new PayoutVerificationResult(
          null,
          null,
          PayoutGatewayStatus.UNKNOWN,
          null,
          null,
          null
      );
    }

    PaystackTransferResponse.Data data = response.getData();

    BigDecimal amount = data.getAmount() != null
        ? BigDecimal.valueOf(data.getAmount()).movePointLeft(2)
        : null;

    return new PayoutVerificationResult(
        data.getReference(),
        data.getTransferCode(),
        mapPayoutStatus(data.getStatus()),
        amount,
        data.getCurrency(),
        data.getTransferredAt()
    );
  }

  public PayoutWebhookEvent parsePayoutWebhook(String payload) {

    try {
      return objectMapper.readValue(
          payload,
          PayoutWebhookEvent.class
      );
    } catch (Exception e) {
      throw new RuntimeException(
          "Unable to parse Paystack payout webhook payload",
          e
      );
    }
  }

//  private PayoutResult mapTransferResponse(
//      PaystackTransferResponse response
//  ) {
//
//    if (response == null || !response.isStatus()) {
//
//      return PayoutResult.builder()
//          .accepted(false)
//          .status(PayoutGatewayStatus.FAILED)
//          .failureReason(
//              response != null
//                  ? response.getMessage()
//                  : "Paystack transfer failed"
//          )
//          .build();
//    }
//
//    return PayoutResult.builder()
//        .accepted(true)
//        .status(
//            mapInitiationStatus(
//                response.getData().getStatus()
//            )
//        )
//        .providerReference(
//            response.getData().getReference()
//        )
//        .providerTransferCode(
//            response.getData().getTransferCode()
//        )
//        .build();
//  }

}
