package com.benkih.estore.user.entity;

import com.benkih.estore.address.entity.Address;
import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.common.enums.UserStatus;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.refreshToken.entity.RefreshToken;
import com.benkih.estore.review.entity.Review;
import com.benkih.estore.role.entity.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
//@ToString
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private String slug;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  @Column(unique = true, nullable = false)
  @NaturalId
  private String email;

  @Column(length = 20)
  private String phoneNumber;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private boolean emailVerified = false;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatus status = UserStatus.PENDING_VERIFICATION;

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<Address> addresses = new ArrayList<>();

  @OneToMany(mappedBy = "user")
  private List<Review> reviews = new ArrayList<>();

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<RefreshToken> refreshTokens = new ArrayList<>();

  @OneToMany(mappedBy = "user")
  private List<Payment> payments = new ArrayList<>();

  private String createdBy;
  private String updatedBy;

  @OneToOne(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private Cart cart;

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  @OrderBy("createdAt DESC")
  private Set<Order> orders = new HashSet<>();
//  private List<Order> orders = new ArrayList<>();

  public String getFullName() {
    return firstName + " " + lastName;
  }


  @ManyToMany(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.MERGE, CascadeType.REFRESH})
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id",referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
  )
  private Set<Role> roles = new HashSet<>();

  @Column(nullable = false,updatable = false)
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @PrePersist
  public void onCreate() {

    if (this.slug == null) {
      this.slug = UUID.randomUUID().toString();
    }
    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
