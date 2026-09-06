package com.benkih.estore.business.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.CurrencyCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "business_balances",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_business_balance_currency",
            columnNames = {
                "business_id",
                "currency"
            }
        )
    },
    indexes = {
        @Index(
            name = "idx_business_balance_business",
            columnList = "business_id"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class BusinessBalance extends BaseEntity {

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
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal pendingBalance = BigDecimal.ZERO;

  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal availableBalance = BigDecimal.ZERO;

  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal reservedBalance = BigDecimal.ZERO;

  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal lifetimeGross = BigDecimal.ZERO;

  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal lifetimeFees = BigDecimal.ZERO;

  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal lifetimePayouts = BigDecimal.ZERO;

  @Column(nullable = false)
  private Instant updatedAt;

  @Version
  private Long version;// for optimistic locking
}
