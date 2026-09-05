package com.benkih.estore.ledger.entity;

import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.ledger.enums.LedgerTransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "ledger_transactions",
    indexes = {
        @Index(
            name = "idx_ledger_transaction_reference",
            columnList = "reference"
        ),
        @Index(
            name = "idx_ledger_transaction_type",
            columnList = "transaction_type"
        ),
        @Index(
            name = "idx_ledger_transaction_created_at",
            columnList = "created_at"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class LedgerTransaction extends AuditableEntity {

  @Enumerated(EnumType.STRING)
  @Column(
      name = "transaction_type",
      nullable = false,
      length = 50
  )
  private LedgerTransactionType type;

  @Enumerated(EnumType.STRING)
  @Column(
      nullable = false,
      length = 3
  )
  private CurrencyCode currency;

  @Column(
      nullable = false,
      unique = true,
      length = 100
  )
  private String reference;

  @Column(length = 500)
  private String description;

  /**
   * Total debit amount.
   */
  @Column(
      name = "total_debit",
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal totalDebit = BigDecimal.ZERO;

  /**
   * Total credit amount.
   */
  @Column(
      name = "total_credit",
      nullable = false,
      precision = 19,
      scale = 2
  )
  private BigDecimal totalCredit = BigDecimal.ZERO;

  @OneToMany(
      mappedBy = "transaction",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<LedgerEntry> entries = new ArrayList<>();
}