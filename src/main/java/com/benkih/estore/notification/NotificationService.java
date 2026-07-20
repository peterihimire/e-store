package com.benkih.estore.notification;

import com.benkih.estore.email.builder.LoginEmailBuilder;
import com.benkih.estore.email.builder.PaymentReceiptEmailBuilder;
import com.benkih.estore.email.builder.VerificationEmailBuilder;
import com.benkih.estore.email.builder.WelcomeEmailBuilder;
import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.service.EmailService;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements INotificationService {
  private final EmailService emailService;
  private final WelcomeEmailBuilder welcomeEmailBuilder;
  private final VerificationEmailBuilder verificationEmailBuilder;
  private final LoginEmailBuilder loginEmailBuilder;
  private final PaymentReceiptEmailBuilder paymentReceiptEmailBuilder;
  //  private final OrderConfirmationEmailBuilder orderConfirmationEmailBuilder;

//  private final SmsService smsService;

//  private final PushService pushService;


  @Override
  public void sendWelcomeEmail(User user) {
    sendEmail(welcomeEmailBuilder.build(user));
  }

  @Override
  public void sendVerificationEmail(User user, String token) {
    sendEmail(verificationEmailBuilder.build(user, token));
  }

  @Override
  public void sendLoginEmail(User user) {
    sendEmail(loginEmailBuilder.build(user));
  }

  @Override
  public void sendPaymentReceipt(Payment payment) {
    sendEmail(paymentReceiptEmailBuilder.build(payment));
  }

  @Override
  public void sendOrderConfirmation(Order order) {
//    sendEmail(orderConfirmationEmailBuilder.build(order));
//    sendSms(orderConfirmationSmsBuilder.build(order));
//    sendPush(orderConfirmationPushBuilder.build(order));
  }

  @Override
  public void sendOrderShipped(Order order) {
    // later
  }

  @Override
  public void sendRefundNotification(Payment payment) {
    // later
  }

  private void sendEmail(EmailRequest request) {
    try {
      emailService.send(request);
    } catch (Exception e) {
      log.error("Unable to send email to {}", request.getTo(), e);
    }
  }

//  private void sendSms(SmsRequest request) {
//    try {
//      smsService.send(request);
//    } catch (Exception e) {
//      log.error("Unable to send SMS to {}", request.getTo(), e);
//    }
//  }
//
//  private void sendPush(PushRequest request) {
//    try {
//      pushService.send(request);
//    } catch (Exception e) {
//      log.error("Unable to send push notification", e);
//    }
//  }
}