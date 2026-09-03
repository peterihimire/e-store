package com.benkih.estore.order.entity;

import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.user.entity.Address;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.enums.OrderStatus;
import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order extends AuditableEntity {

  @Column(nullable = false, unique = true)
  private String orderNumber;

  @Column(nullable = false)
  private Instant orderDate;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal subTotal = BigDecimal.ZERO;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal totalAmount = BigDecimal.ZERO;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal shippingFee = BigDecimal.ZERO;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CurrencyCode currency = CurrencyCode.NGN;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus orderStatus = OrderStatus.PENDING;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus paymentStatus = PaymentStatus.PENDING;

  private String trackingNumber;
  private String notes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shipping_address_id")
  private Address shippingAddress;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "billing_address_id")
  private Address billingAddress;

  @OneToMany(
      mappedBy = "order",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<OrderItem> orderItems = new ArrayList<>();
  //  private Set<OrderItem> items = new HashSet<>();

  @OneToMany(
      mappedBy = "order",
      cascade = CascadeType.ALL
  )
  private List<Payment> payments = new ArrayList<>();

  private Instant shippedAt;

  private Instant deliveredAt;

  private Instant paidAt;

  private Instant cancelledAt;

  private Instant completedAt;

  @PrePersist
  protected void initializeOrder() {

    if (orderNumber == null) {
      orderNumber = "ORD-" + System.currentTimeMillis();
    }

    if (orderDate == null) {
      orderDate = Instant.now();
    }
  }
}
