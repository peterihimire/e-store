package com.benkih.estore.business.entity;


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
@Table(name = "customer_addresses")
public class CustomerAddress extends BaseEntity {

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

  private String phoneNumber;

  @Column(nullable = false)
  private boolean defaultAddress;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  private String createdBy;
  private String updatedBy;

}
