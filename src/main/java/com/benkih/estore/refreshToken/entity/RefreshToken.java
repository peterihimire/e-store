package com.benkih.estore.refreshToken.entity;

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
@Table(name = "refresh_tokens")
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private String slug;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, unique = true, length = 500)
  private String token;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Column(nullable = false)
  private Boolean revoked = false;

  @Column(nullable = false)
  private Boolean expired = false;

  @Column(length = 100)
  private String device;

  @Column(length = 45)
  private String ipAddress;

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


//package com.benkih.estore.refreshTokens.entity;
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
//@Table(name = "refreshTokens")
//public class RefreshToken {
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  private Long id;
//
//  @Column(nullable = false, unique = true, updatable = false)
//  private String slug;
//
//
//
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
