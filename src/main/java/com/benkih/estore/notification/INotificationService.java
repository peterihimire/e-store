package com.benkih.estore.notification;

//package com.benkih.estore.notification.service;

import com.benkih.estore.auth.entity.UserInvitation;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.user.entity.User;

public interface INotificationService {

  void sendWelcomeEmail(User user);

  void sendVerificationEmail(User user, String token);

  void sendLoginEmail(User user);

  void sendPaymentReceipt(Payment payment);

  void sendOrderConfirmation(Order order);

  void sendOrderProcessing(Order order);

  void sendOrderCancelled(Order order);

  void sendOrderShipped(Order order);

  void sendOrderDelivered(Order order);

  void sendRefundNotification(Payment payment);

  void sendPasswordReset(User user, String token);

  void sendInvitationEmail(UserInvitation user, String token);
}
