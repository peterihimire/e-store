package com.benkih.estore.settlement.entity;



import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.enums.CurrencyCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
    name = "settlement_items",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_settlement_allocation",
            columnNames = {
                "settlement_id",
                "allocation_id"
            }
        )
    },
    indexes = {
        @Index(
            name = "idx_settlement_item_allocation",
            columnList = "allocation_id"
        ),
        @Index(
            name = "idx_settlement_item_business",
            columnList = "business_id"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class SettlementItem extends AuditableEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "settlement_id",
      nullable = false
  )
  private Settlement settlement;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "allocation_id",
      nullable = false
  )
  private Allocation allocation;

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

//import com.benkih.estore.allocation.entity.Allocation;
//import com.benkih.estore.common.entity.BaseEntity;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.math.BigDecimal;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@Entity
//@Table(
//    name = "settlement_items",
//    uniqueConstraints = {
//        @UniqueConstraint(
//            name = "uk_settlement_allocation",
//            columnNames = {"settlement_id", "allocation_id"}
//        )
//    },
//    indexes = {
//        @Index(name = "idx_settlement_item_settlement", columnList = "settlement_id"),
//        @Index(name = "idx_settlement_item_allocation", columnList = "allocation_id")
//    }
//)
//public class SettlementItem extends BaseEntity {
//
//  @ManyToOne(fetch = FetchType.LAZY, optional = false)
//  @JoinColumn(name = "settlement_id", nullable = false)
//  private Settlement settlement;
//
//  @ManyToOne(fetch = FetchType.LAZY, optional = false)
//  @JoinColumn(name = "allocation_id", nullable = false)
//  private Allocation allocation;
//
//  @Column(
//      name = "amount",
//      nullable = false,
//      precision = 18,
//      scale = 2
//  )
//  private BigDecimal amount = BigDecimal.ZERO;
//}
