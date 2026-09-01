package com.benkih.estore.user.entity;

import com.benkih.estore.common.entity.AuditableEntity;
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
@Table(name = "addresses")
public class Address extends AuditableEntity {

  @Column(nullable = false) // need to update this to use first and last names
  private String firstName;

  @Column(nullable = false) // need to update this to use first and last names
  private String lastName;

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
  private boolean defaultAddress;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

}
