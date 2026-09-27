package com.benkih.estore.business.entity;

import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.payout.entity.PayoutRecipient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@NoArgsConstructor
public class BankAccount extends AuditableEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

  @Column(nullable = false)
  private String bankCode;

  @Column(nullable = false)
  private String bankName;

  @Column(nullable = false)
  private String accountNumber;

  @Column(nullable = false)
  private String accountName;

  @Column(nullable = false)
  private boolean verified = false;

  @Column(nullable = false)
  private boolean defaultAccount = false;

  @OneToMany(
      mappedBy = "bankAccount",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<PayoutRecipient> payoutRecipients = new ArrayList<>();
}
