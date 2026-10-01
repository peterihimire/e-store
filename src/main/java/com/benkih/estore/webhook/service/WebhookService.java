package com.benkih.estore.webhook.service;

import com.benkih.estore.audit.service.ApiLogService;
import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.payment.service.PaymentService;
import com.benkih.estore.payout.service.PayoutService;
import com.benkih.estore.refund.service.RefundService;
import com.benkih.estore.webhook.handler.WebhookEvent;
import com.benkih.estore.webhook.handler.IWebhookHandler;
//import com.benkih.estore.webhook.handler.WebhookHandler;
import com.benkih.estore.webhook.handler.WebhookHandlerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {
    private final WebhookHandlerFactory webhookHandlerFactory;

    private final PaymentService paymentService;
    private final PayoutService payoutService;
    private final RefundService refundService;

    private final ApiLogService apiLogService;

  @Transactional
  public void handlePaystackWebhook(
      String signature,
      String payload,
      String endpoint
  ) {

    try {

      IWebhookHandler handler =
          webhookHandlerFactory.get(
              PaymentProvider.PAYSTACK
          );

      handler.verifySignature(
          signature,
          payload
      );

      WebhookEvent event =
          handler.parseWebhook(payload);

      routeEvent(
          event,
          signature,
          payload
      );

      apiLogService.saveInboundLog(
          "POST",
          endpoint,
          payload,
          200,
          "Webhook processed successfully",
          null
      );

    } catch (Exception e) {

      apiLogService.saveInboundLog(
          "POST",
          endpoint,
          payload,
          500,
          null,
          e
      );

      throw e;
    }
  }


  private void routeEvent(
      WebhookEvent event,
      String signature,
      String payload
  ) {

    switch (event.eventType()) {

     // PAYMENT
      case "charge.success":
      case "charge.failed":

        paymentService.handleWebhook(
            event,
            signature,
            payload
        );
        break;

     // PAYOUT / TRANSFER
      case "transfer.success":
      case "transfer.failed":
      case "transfer.reversed":

        payoutService.handleWebhook(
            event,
            signature,
            payload
        );
        break;

  // REFUND
      case "refund.pending":
      case "refund.processing":
      case "refund.needs-attention":
      case "refund.failed":
      case "refund.processed":

        refundService.handleWebhook(
            event,
            signature,
            payload
        );
        break;

      // UNKNOWN
      default:
        log.info("Ignoring event {}", event.eventType());
    }
  }
  }

