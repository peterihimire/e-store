package com.benkih.estore.settlement.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.settlement.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "settlements",
    indexes = {
        @Index(
            name = "idx_settlement_business",
            columnList = "business_id"
        ),
        @Index(
            name = "idx_settlement_status",
            columnList = "status"
        ),
        @Index(
            name = "idx_settlement_period",
            columnList = "period_start,period_end"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Settlement extends AuditableEntity {

  @Column(name = "settlement_number", nullable = false, unique = true)
  private String settlementNumber;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "business_id",
      nullable = false
  )
  private Business business;

  @Enumerated(EnumType.STRING)
  @Column(
      nullable = false,
      length = 3
  )
  private CurrencyCode currency;

  @Column(
      name = "period_start",
      nullable = false
  )
  private Instant periodStart;

  @Column(
      name = "period_end",
      nullable = false
  )
  private Instant periodEnd;

  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal grossAmount = BigDecimal.ZERO;

  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal platformFee = BigDecimal.ZERO;

  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal paymentFee = BigDecimal.ZERO;

  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal shippingAmount = BigDecimal.ZERO;

  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal refundAmount = BigDecimal.ZERO;

  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal adjustmentAmount = BigDecimal.ZERO;

  /**
   * Actual amount payable to seller.
   */
  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal netAmount = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(
      nullable = false,
      length = 20
  )
  private SettlementStatus status = SettlementStatus.PENDING;

  private Instant eligibleAt;

  private Instant settledAt;

  @Column(name = "failure_reason", length = 500)
  private String failureReason;

  @OneToMany(
      mappedBy = "settlement",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<SettlementItem> items = new ArrayList<>();
}


//import com.benkih.estore.business.entity.Business;
//import com.benkih.estore.common.entity.AuditableEntity;
//import com.benkih.estore.common.enums.CurrencyCode;
//import com.benkih.estore.settlement.enums.SettlementStatus;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@Entity
//@Table(
//    name = "settlements",
//    indexes = {
//        @Index(name = "idx_settlement_business", columnList = "business_id"),
//        @Index(name = "idx_settlement_status", columnList = "status"),
//        @Index(name = "idx_settlement_eligible_at", columnList = "eligible_at")
//    }
//)
//public class Settlement extends AuditableEntity {
//
//  @Column(name = "settlement_number", nullable = false, unique = true)
//  private String settlementNumber;
//
//  @ManyToOne(fetch = FetchType.LAZY, optional = false)
//  @JoinColumn(name = "business_id", nullable = false)
//  private Business business;
//
//  @Column(
//      name = "gross_amount",
//      nullable = false,
//      precision = 18,
//      scale = 2
//  )
//  private BigDecimal grossAmount = BigDecimal.ZERO;
//
//  @Column(
//      name = "adjustment_amount",
//      nullable = false,
//      precision = 18,
//      scale = 2
//  )
//  private BigDecimal adjustmentAmount = BigDecimal.ZERO;
//
//  @Column(
//      name = "net_amount",
//      nullable = false,
//      precision = 18,
//      scale = 2
//  )
//  private BigDecimal netAmount = BigDecimal.ZERO;
//
//  @Enumerated(EnumType.STRING)
//  @Column(nullable = false, length = 20)
//  private SettlementStatus status = SettlementStatus.PENDING;
//
//  @Enumerated(EnumType.STRING)
//  @Column(nullable = false, length = 3)
//  private CurrencyCode currency = CurrencyCode.NGN;
//
//  @Column(name = "eligible_at", nullable = false)
//  private Instant eligibleAt;
//
//  @Column(name = "settled_at")
//  private Instant settledAt;
//
//  @Column(name = "failure_reason", length = 500)
//  private String failureReason;
//
//  @OneToMany(
//      mappedBy = "settlement",
//      cascade = CascadeType.ALL,
//      orphanRemoval = true
//  )
//  private List<SettlementItem> items = new ArrayList<>();
//}