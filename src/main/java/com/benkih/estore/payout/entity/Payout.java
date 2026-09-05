package com.benkih.estore.payout.entity;


import com.benkih.estore.business.entity.BankAccount;
import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.payout.enums.PayoutStatus;
import com.benkih.estore.settlement.entity.Settlement;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "payouts",
    indexes = {
        @Index(name = "idx_payout_business", columnList = "business_id"),
        @Index(name = "idx_payout_bank_account", columnList = "bank_account_id"),
        @Index(name = "idx_payout_status", columnList = "status"),
        @Index(name = "idx_payout_provider_reference", columnList = "provider_reference")
    }
)
public class Payout extends AuditableEntity {

  @Column(name = "payout_number", nullable = false, unique = true)
  private String payoutNumber;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "settlement_id", nullable = false)
  private Settlement settlement;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)

  @JoinColumn(name = "bank_account_id", nullable = false)

  private BankAccount bankAccount;

  @Column(name = "account_name", nullable = false)
  private String accountName;

  @Column(name = "account_number", nullable = false, length = 20)
  private String accountNumber;

  @Column(name = "bank_code", nullable = false, length = 20)
  private String bankCode;

  @Column(name = "bank_name", nullable = false)
  private String bankName;

  @Column(
      name = "gross_amount",
      nullable = false,
      precision = 18,
      scale = 2
  )
  private BigDecimal grossAmount = BigDecimal.ZERO;

  @Column(
      name = "transfer_fee",
      nullable = false,
      precision = 18,
      scale = 2
  )
  private BigDecimal transferFee = BigDecimal.ZERO;

  @Column(
      name = "stamp_duty",
      nullable = false,
      precision = 18,
      scale = 2
  )
  private BigDecimal stampDuty = BigDecimal.ZERO;

  @Column(
      name = "net_amount",
      nullable = false,
      precision = 18,
      scale = 2
  )
  private BigDecimal netAmount = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PayoutStatus status = PayoutStatus.PENDING;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private CurrencyCode currency = CurrencyCode.NGN;

  @Column(name = "provider_reference", unique = true)
  private String providerReference;

  @Column(name = "failure_reason", length = 500)
  private String failureReason;

  @Column(name = "processed_at")
  private Instant processedAt;
}
