package com.benkih.estore.user.entity;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private String slug;

  private String firstName;
  private String LastName;

  @Column(unique = true, nullable = false)
  @NaturalId
  private String email;
  private String password;

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
  private Set<Order> orders = new HashSet<>();
  //  private List<Order> orders;

  @Column(nullable = false,updatable = false)
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @PrePersist
  public void onCreate() {
    // this.slug = UUID.randomUUID().toString();

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
