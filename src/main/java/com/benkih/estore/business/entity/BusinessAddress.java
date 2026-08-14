package com.benkih.estore.business.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.Country;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "business_addresses")
public class BusinessAddress extends BaseEntity {

  @Column(nullable = false)
  private String addressName;

  @Column(nullable = false)
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Country country = Country.NIGERIA;

  @Column(nullable = false)
  private String addressLine1;

  private String addressLine2;

  @Column(nullable = false)
  private String city;

  private String state;

  private String postalCode;

  @Column(nullable = false)
  private boolean defaultAddress = false;

  private String createdBy;
  private String updatedBy;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

}