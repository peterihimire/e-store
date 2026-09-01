package com.benkih.estore.payment.entity;

import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.PaymentEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payment_events")
public class PaymentEvent extends AuditableEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "payment_id", nullable = false)
  private Payment payment;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentEventType eventType;

 // JSON payload received from Paystack.
  @Lob
  @Column(columnDefinition = "TEXT")
  private String payload;
}