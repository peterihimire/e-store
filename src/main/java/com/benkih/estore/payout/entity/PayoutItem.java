package com.benkih.estore.payout.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.settlement.entity.Settlement;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
    name = "payout_items",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_payout_settlement",
            columnNames = {
                "payout_id",
                "settlement_id"
            }
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class PayoutItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "payout_id",
      nullable = false
  )
  private Payout payout;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "settlement_id",
      nullable = false
  )
  private Settlement settlement;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "business_id",
      nullable = false
  )
  private Business business;

  @Column(
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(
      nullable = false,
      length = 3
  )
  private CurrencyCode currency;
}
