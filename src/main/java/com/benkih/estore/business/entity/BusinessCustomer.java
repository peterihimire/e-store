package com.benkih.estore.business.entity;


import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.user.entity.Address;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
  @Table(name = "business_customers")
  @Getter
  @Setter
  @NoArgsConstructor
  public class BusinessCustomer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String name;

    private String email;
    private String phone;
  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<Address> addresses = new ArrayList<>();

  }
