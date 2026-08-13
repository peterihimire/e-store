package com.benkih.estore.business.entity;

import com.benkih.estore.business.enums.BusinessStatus;
import com.benkih.estore.business.enums.BusinessType;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.user.entity.Address;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "businesses")
public class Business extends BaseEntity {

  @Column(nullable = false)
  private String name;

  private String legalName;

  @Column(nullable = false)
  private String email;

  private String phone;
  private String logoUrl;
  private String website;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BusinessStatus status = BusinessStatus.PENDING;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BusinessType type = BusinessType.COMPANY;

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<Address> addresses = new ArrayList<>();
}