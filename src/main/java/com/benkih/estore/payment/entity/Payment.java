package com.benkih.estore.payment.entity;

import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.CurrencyCode;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment extends AuditableEntity {

  @Column(nullable = false, unique = true)
  private String reference;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CurrencyCode currency = CurrencyCode.NGN;

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

  private Instant paidAt;

  @Column(
      name = "processor_fee",
      nullable = false,
      precision = 18,
      scale = 2
  )
  private BigDecimal processorFee = BigDecimal.ZERO;

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

}
