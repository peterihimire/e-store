package com.benkih.estore.review.entity;

import com.benkih.estore.product.entity.Product;
import com.benkih.estore.user.entity.User;
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
@Table(
    name = "reviews",
    uniqueConstraints = { // This unique constraint is to enforce  one review per user per product at the database level
        @UniqueConstraint(
            name = "uk_review_user_product",
            columnNames = {"user_id", "product_id"}
        )
    }
)
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private String slug;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(nullable = false)
  private Integer rating;

  @Column(length = 1000)
  private String comment;

  @Column(nullable = false)
  private Boolean verifiedPurchase = false;

  private String createdBy;
  private String updatedBy;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @PrePersist
  public void onCreate() {
    if (slug == null) {
      slug = UUID.randomUUID().toString();
    }

    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  public void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}

//package com.benkih.estore.review.entity;
//
//import com.benkih.estore.common.enums.Currency;
//import com.benkih.estore.common.enums.PaymentMethod;
//import com.benkih.estore.common.enums.PaymentStatus;
//import com.benkih.estore.order.entity.Order;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@Entity
//@Table(name = "reviews")
//public class Review {
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  private Long id;
//
//  @Column(nullable = false, unique = true, updatable = false)
//  private String slug;
//
//  private String createdBy;
//  private String updatedBy;
//
//  @Column(nullable = false, updatable = false)
//  private LocalDateTime createdAt;
//
//  private LocalDateTime updatedAt;
//
//  @PrePersist
//  public void onCreate() {
//    if (slug == null) {
//      slug = UUID.randomUUID().toString();
//    }
//
//    if (createdAt == null) {
//      createdAt = LocalDateTime.now();
//    }
//  }
//
//  @PreUpdate
//  public void onUpdate() {
//    updatedAt = LocalDateTime.now();
//  }
//}
