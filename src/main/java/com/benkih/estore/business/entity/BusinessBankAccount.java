package com.benkih.estore.business.entity;

import com.benkih.estore.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "business_bank_accounts")
@Getter
@Setter
@NoArgsConstructor
public class BusinessBankAccount extends BaseEntity {

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
}
