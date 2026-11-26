package com.benkih.estore.notification;

import com.benkih.estore.auth.entity.UserInvitation;
import com.benkih.estore.email.builder.InvitationEmailBuilder;
import com.benkih.estore.email.builder.LoginEmailBuilder;
import com.benkih.estore.email.builder.OrderCancelledEmailBuilder;
import com.benkih.estore.email.builder.OrderConfirmationEmailBuilder;
import com.benkih.estore.email.builder.OrderDeliveredEmailBuilder;
import com.benkih.estore.email.builder.OrderProcessingEmailBuilder;
import com.benkih.estore.email.builder.OrderShippedEmailBuilder;
import com.benkih.estore.email.builder.PasswordResetEmailBuilder;
import com.benkih.estore.email.builder.PaymentReceiptEmailBuilder;
import com.benkih.estore.email.builder.VerificationEmailBuilder;
import com.benkih.estore.email.builder.WelcomeEmailBuilder;
import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.notification.dispatch.NotificationDispatchEvent;
import com.benkih.estore.notification.dispatch.NotificationDispatchService;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationDispatchService dispatchService;
    private final ApplicationEventPublisher eventPublisher;
    private final WelcomeEmailBuilder welcomeEmailBuilder;
    private final VerificationEmailBuilder verificationEmailBuilder;
    private final LoginEmailBuilder loginEmailBuilder;
    private final PaymentReceiptEmailBuilder paymentReceiptEmailBuilder;
    private final OrderConfirmationEmailBuilder orderConfirmationEmailBuilder;
    private final OrderProcessingEmailBuilder orderProcessingEmailBuilder;
    private final OrderShippedEmailBuilder orderShippedEmailBuilder;
    private final OrderCancelledEmailBuilder orderCancelledEmailBuilder;
    private final OrderDeliveredEmailBuilder orderDeliveredEmailBuilder;
    private final PasswordResetEmailBuilder passwordResetEmailBuilder;
    private final InvitationEmailBuilder invitationEmailBuilder;

    @Override
    public void sendWelcomeEmail(User user) {
        dispatch(
                "welcome:" + user.getSlug(),
                "user.welcome",
                "welcome",
                welcomeEmailBuilder.build(user));
    }

    @Override
    public void sendVerificationEmail(User user, String token) {
        dispatch(
                "verification:" + token,
                "user.verification",
                "verifyEmail",
                verificationEmailBuilder.build(user, token));
    }

    @Override
    public void sendLoginEmail(User user) {
        dispatch("login:" + user.getSlug(), "user.login", "login", loginEmailBuilder.build(user));
    }

    @Override
    public void sendPaymentReceipt(Payment payment) {
        dispatch(
                "payment-receipt:" + payment.getReference(),
                "payment.receipt",
                "paymentReceipt",
                paymentReceiptEmailBuilder.build(payment));
    }

    @Override
    public void sendOrderConfirmation(Order order) {
        dispatch(
                "order-confirmation:" + order.getSlug(),
                "order.confirmation",
                "orderConfirmation",
                orderConfirmationEmailBuilder.build(order));
    }

    @Override
    public void sendOrderProcessing(Order order) {
        dispatch(
                "order-processing:" + order.getSlug(),
                "order.processing",
                "orderProcessing",
                orderProcessingEmailBuilder.build(order));
    }

    @Override
    public void sendOrderCancelled(Order order) {
        dispatch(
                "order-cancelled:" + order.getSlug(),
                "order.cancelled",
                "orderCancelled",
                orderCancelledEmailBuilder.build(order));
    }

    @Override
    public void sendOrderShipped(Order order) {
        dispatch(
                "order-shipped:" + order.getSlug(),
                "order.shipped",
                "orderShipped",
                orderShippedEmailBuilder.build(order));
    }

    @Override
    public void sendOrderDelivered(Order order) {
        dispatch(
                "order-delivered:" + order.getSlug(),
                "order.delivered",
                "orderDelivered",
                orderDeliveredEmailBuilder.build(order));
    }

    @Override
    public void sendRefundNotification(Payment payment) {
        // No refund email template yet.
    }

    @Override
    public void sendPasswordReset(User user, String token) {
        dispatch(
                "password-reset:" + token,
                "user.password-reset",
                "passwordReset",
                passwordResetEmailBuilder.build(user, token));
    }

    @Override
    public void sendInvitationEmail(UserInvitation invitation, String token) {
        dispatch(
                "invitation:" + invitation.getSlug(),
                "user.invitation",
                "invitation",
                invitationEmailBuilder.build(invitation, token));
    }

    private void dispatch(
            String eventKey, String eventType, String template, EmailRequest request) {
        try {
            // Written in the caller's transaction. The listener sends it only after that
            // transaction
            // commits, so a rolled-back order/payment cannot produce a confirmation email.
            Long notificationId = dispatchService.prepare(eventKey, eventType, template, request);
            if (notificationId != null) {
                eventPublisher.publishEvent(new NotificationDispatchEvent(notificationId));
            }
        } catch (Exception e) {
            // Notification persistence must not roll back the business operation that triggered it.
            log.error(
                    "Unable to prepare email event {} to {}",
                    eventType,
                    request == null ? null : request.getTo(),
                    e);
        }
    }
}
