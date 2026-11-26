package com.benkih.estore.refund.service;

import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.common.enums.RefundGatewayStatus;
import com.benkih.estore.common.enums.RefundStatus;
import com.benkih.estore.common.exceptions.PaymentException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.repository.OrderRepository;
import com.benkih.estore.payment.dto.request.RefundPaymentRequest;
import com.benkih.estore.payment.dto.response.RefundPaymentResponse;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.payment.provider.PaymentGateway;
import com.benkih.estore.payment.provider.PaymentGatewayFactory;
import com.benkih.estore.payment.repository.PaymentRepository;
import com.benkih.estore.refund.entity.Refund;
import com.benkih.estore.refund.repository.RefundRepository;
import com.benkih.estore.webhook.handler.WebhookEvent;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundGatewayService {
    private final PaymentGatewayFactory gatewayFactory;
    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RefundQueryService refundQueryService;
    private final RefundSellerSettlementService refundSellerSettlementService;

    @Transactional
    public RefundPaymentResponse initializeRefund(String refundSlug) {

        Refund refund = refundQueryService.getRefundEntity(refundSlug);

        if (refund.getRefundStatus() != RefundStatus.APPROVED) {
            throw new PaymentException("Refund must be approved before it can be initialized.");
        }

        if (refund.getGatewayStatus() == RefundGatewayStatus.PROCESSED) {
            throw new PaymentException("Refund has already been processed.");
        }

        Payment payment = refund.getPayment();
        PaymentGateway gateway = gatewayFactory.get(payment.getPaymentProvider());
        log.info(
                "Refund peter: id={}, slug={}, amount={}, status={}, "
                        + "gatewayStatus={}, gatewayRefundId={}, reason={}",
                refund.getId(),
                refund.getSlug(),
                refund.getAmount(),
                refund.getRefundStatus(),
                refund.getGatewayStatus(),
                refund.getGatewayRefundId(),
                refund.getReason());

        log.info(
                "Payment peter: id={}, slug={}, reference={}, transactionId={}, "
                        + "amount={}, provider={}, status={}",
                payment.getId(),
                payment.getSlug(),
                payment.getReference(),
                payment.getTransactionId(),
                payment.getAmount(),
                payment.getPaymentProvider(),
                payment.getPaymentStatus());

        BigDecimal amountInKobo = refund.getAmount().multiply(BigDecimal.valueOf(100));

        RefundPaymentRequest request =
                RefundPaymentRequest.builder()
                        .transactionReference(payment.getReference())
                        .amount(amountInKobo)
                        .reason(refund.getReason())
                        .build();

        RefundPaymentResponse response = gateway.refund(request);

        if (!response.isSuccess()) {
            refund.setGatewayStatus(RefundGatewayStatus.FAILED);
            refund.setFailureReason(response.getMessage());
            refundRepository.save(refund);

            throw new PaymentException("Refund initialization failed: " + response.getMessage());
        }

        refund.setGatewayStatus(response.getStatus());
        refund.setGatewayRefundId(response.getRefundReference());
        refund.setGatewayResponse(response.getMessage());

        refundRepository.save(refund);

        return response;
    }

    @Transactional
    public RefundPaymentResponse verifyRefund(String refundReference) {

        Refund refund =
                refundRepository
                        .findByReference(refundReference)
                        .orElseThrow(() -> new ResourceNotFoundException("Refund not found"));

        PaymentGateway gateway = gatewayFactory.get(refund.getProvider());
        RefundPaymentResponse response = gateway.verifyRefund(refundReference);

        if (response == null) {
            throw new PaymentException("Unable to verify refund status.");
        }

        refund.setGatewayStatus(response.getStatus());

        if (response.getRefundReference() != null) {
            refund.setGatewayRefundId(response.getRefundReference());
        }
        refund.setGatewayResponse(response.getMessage());

        if (response.getStatus() == RefundGatewayStatus.PROCESSED
                || response.getStatus() == RefundGatewayStatus.SUCCESS) {
            refund.setRefundedAt(Instant.now());
        }

        refundRepository.save(refund);

        return response;
    }

    // WEBHOOK STATUS UPDATES
    @Transactional
    public void markPending(WebhookEvent event) {
        Refund refund =
                refundQueryService.getRefundByPaymentReference(event.transactionReference());
        refund.setGatewayStatus(RefundGatewayStatus.PENDING);

        refundRepository.save(refund);
    }

    @Transactional
    public void markProcessing(WebhookEvent event) {
        Refund refund =
                refundQueryService.getRefundByPaymentReference(event.transactionReference());
        refund.setGatewayStatus(RefundGatewayStatus.PROCESSING);

        refundRepository.save(refund);
    }

    @Transactional
    public void markNeedsAttention(WebhookEvent event) {
        Refund refund =
                refundQueryService.getRefundByPaymentReference(event.transactionReference());
        refund.setGatewayStatus(RefundGatewayStatus.NEEDS_ATTENTION);

        refundRepository.save(refund);
        // Notification can be added here later.
        log.info("Refund {} requires customer attention", event.transactionReference());
    }

    @Transactional
    public void markSuccessful(WebhookEvent event) {

        Refund refund =
                refundQueryService.getRefundByPaymentReference(event.transactionReference());

        Payment payment = refund.getPayment();
        Order order = payment.getOrder();

        BigDecimal refundedAmount = event.amount();

        if (refundedAmount.compareTo(refund.getAmount()) != 0) {
            throw new PaymentException("Gateway refund amount does not match refund amount.");
        }

        refund.setGatewayStatus(RefundGatewayStatus.PROCESSED);
        refund.setRefundedAt(Instant.now());

        refundRepository.flush(); // see note below

        BigDecimal totalRefunded =
                refundRepository.sumRefundsByPaymentId(
                        payment.getId(), RefundGatewayStatus.PROCESSED);

        BigDecimal paidAmount = payment.getAmount();

        if (totalRefunded.compareTo(paidAmount) > 0) {
            refund.setGatewayStatus(
                    RefundGatewayStatus
                            .NEEDS_ATTENTION); // maybe send message to the admin or something

            refund.setFailureReason("Total refunded amount exceeds payment amount.");

            log.error(
                    "OVER-REFUND detected. Payment={}, paid={}, refunded={}",
                    payment.getReference(),
                    paidAmount,
                    totalRefunded);

            return;
        }

        if (totalRefunded.compareTo(paidAmount) == 0) {
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            payment.setPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }
        updateOrderPaymentStatus(order, payment.getPaymentStatus());
        paymentRepository.save(payment);
        orderRepository.save(order);

        refundSellerSettlementService.recordSellerRefund(refund, payment);

        log.info(
                "Refund {} processed for payment {}. Paid={}, totalRefunded={}, order={}",
                refund.getReference(),
                payment.getReference(),
                paidAmount,
                totalRefunded,
                order.getOrderNumber());
    }

    @Transactional
    public void markFailed(WebhookEvent event, String reason) {
        Refund refund =
                refundQueryService.getRefundByPaymentReference(event.transactionReference());
        refund.setGatewayStatus(RefundGatewayStatus.FAILED);
        refund.setFailureReason(reason);

        refundRepository.save(refund);
    }

    @Transactional
    public void handleWebhook(WebhookEvent event, String signature, String payload) {

        switch (event.eventType()) {
            case "refund.pending":
                markPending(event);
                break;

            case "refund.processing":
                markProcessing(event);
                break;

            case "refund.needs-attention":
                markNeedsAttention(event);
                break;

            case "refund.failed":
                markFailed(event, "Refund failed");
                break;

            case "refund.processed":
                markSuccessful(event);
                break;

            default:
                log.info("Ignoring refund event: {}", event.eventType());
        }
    }

    private void updateOrderPaymentStatus(Order order, PaymentStatus paymentStatus) {
        order.setPaymentStatus(paymentStatus);
    }
}

// NOTE:
// Also, your refundRepository.flush() is different from save() here.
// You need the flush because you’re immediately querying the database
// for sumRefundsByPaymentId() and you want the just-marked refund included in that SUM.
// You don’t need to flush the payment before reading its status because you’re
// reading the Java object, not querying the database.

//  @Override
//  @Transactional
//  public void markSuccessful(PaymentWebhookEvent event) {
//    Refund refund = getRefundByPaymentReference(event.transactionReference());
//    refund.setGatewayStatus(RefundGatewayStatus.PROCESSED);
//    refund.setRefundedAt(LocalDateTime.now());
//
//
//    refundRepository.save(refund);
//    log.info(
//        "Refund {} successfully processed for payment {}",
//        refund.getReference(),
//        event.transactionReference()
//    );
//  }
