package com.benkih.estore.business.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "business_settings")
@Getter
@Setter
@NoArgsConstructor
public class BusinessSetting extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "business_id", nullable = false, unique = true)
  private Business business;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Currency currency = Currency.NGN;

  private String timezone;

  private String orderPrefix;
  private String invoicePrefix;

  private boolean invoiceEnabled = false;
}
