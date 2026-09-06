package com.benkih.estore.ledger.entity;


import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.ledger.enums.LedgerAccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "ledger_accounts",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_ledger_account_business_type_currency",
            columnNames = {
                "business_id",
                "account_type",
                "currency"
            }
        )
    },
    indexes = {
        @Index(
            name = "idx_ledger_account_business",
            columnList = "business_id"
        ),
        @Index(
            name = "idx_ledger_account_type",
            columnList = "account_type"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class LedgerAccount extends AuditableEntity {

// Null means this is a platform/system account.
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "business_id")
  private Business business;

  @Enumerated(EnumType.STRING)
  @Column(
      name = "account_type",
      nullable = false,
      length = 50
  )
  private LedgerAccountType accountType;

  @Enumerated(EnumType.STRING)
  @Column(
      nullable = false,
      length = 3
  )
  private CurrencyCode currency;

  @Column(nullable = false)
  private boolean active = true;

  @Column(length = 255)
  private String description;

  public LedgerAccount(
      Business business,
      LedgerAccountType accountType,
      CurrencyCode currency
  ) {
    this.business = business;
    this.accountType = accountType;
    this.currency = currency;
  }
}