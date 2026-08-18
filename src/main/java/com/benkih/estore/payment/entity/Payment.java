package com.benkih.estore.payment.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.enums.Currency;
import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.common.enums.PaymentMethod;
import com.benkih.estore.common.enums.PaymentStatus;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private String slug;

  @Column(nullable = false, unique = true)
  private String reference;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Currency currency = Currency.NGN;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentMethod paymentMethod = PaymentMethod.CARD;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus paymentStatus = PaymentStatus.PENDING;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentProvider paymentProvider = PaymentProvider.PAYSTACK;

  /**
   * Transaction ID returned by Paystack
   */
  @Column(unique = true)
  private String transactionId;

  /**
   * Authorization code returned by Paystack
   */
  private String authorizationCode;

  @Column(length = 500)
  private String authorizationUrl;

  private String accessCode;

  @Column(columnDefinition = "TEXT")
  private String gatewayRequest;

  /**
   * Gateway response
   * e.g. "Successful"
   */
  @Column(columnDefinition = "TEXT")
  private String gatewayResponse;

  @Column(columnDefinition = "TEXT")
  private String gatewayReference;

  /**
   * Optional failure reason
   */
  @Column(columnDefinition = "TEXT")
  private String failureReason;

  private LocalDateTime paidAt;

  private String createdBy;
  private String updatedBy;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @OneToMany(
      mappedBy = "payment",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<PaymentEvent> events = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;


  @PrePersist
  public void onCreate() {
    if (slug == null) {
      slug = UUID.randomUUID().toString();
    }

    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  public void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
