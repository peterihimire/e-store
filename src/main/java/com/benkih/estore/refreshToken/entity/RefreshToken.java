//package com.benkih.estore.refreshToken.entity;
//
//import com.benkih.estore.user.entity.User;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//
//@Entity
//@Table(
//    name = "refresh_tokens",
//    indexes = {
//        @Index(name = "idx_refresh_user", columnList = "user_id"),
//        @Index(name = "idx_refresh_expires", columnList = "expiresAt")
//    }
//)
//@Getter
//@Setter
//@NoArgsConstructor
//public class RefreshToken {
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  private Long id;
//
//  @Column(nullable = false, unique = true, updatable = false)
//  private String slug;
//
//  @Column(nullable = false)
//  private String tokenHash;
//
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "user_id", nullable = false)
//  private User user;
//
//  @Column(nullable = false)
//  private LocalDateTime expiresAt;
//
//  @Column(nullable = false)
//  private boolean revoked = false;
//
//  private LocalDateTime lastUsedAt;
//
//  private LocalDateTime revokedAt;
//
//  private String revokedReason;
//
//  @Column(length = 255)
//  private String device;
//
//  private String ipAddress;
//
//
//  @Column(nullable = false,updatable = false)
//  private LocalDateTime createdAt;
//  private LocalDateTime updatedAt;
//
//  public boolean isExpired() {
//    return !expiresAt.isAfter(LocalDateTime.now());
//  }
//
//  @PrePersist
//  public void onCreate() {
//    if (this.slug == null) {
//      this.slug = UUID.randomUUID().toString();
//    }
//
//    if (this.createdAt == null) {
//      this.createdAt = LocalDateTime.now();
//    }
//  }
//
//  @PreUpdate
//  public void onUpdate() {
//    this.updatedAt = LocalDateTime.now();
//  }
//}


//package com.benkih.estore.refreshToken.entity;
//
//import com.benkih.estore.user.entity.User;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@Entity
//@Table(name = "refresh_tokens")
//public class RefreshToken {
//
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  private Long id;
//
//  @Column(nullable = false, unique = true, updatable = false)
//  private String slug;
//
//  @ManyToOne(fetch = FetchType.LAZY, optional = false)
//  @JoinColumn(name = "user_id", nullable = false)
//  private User user;
//
//  @Column(nullable = false, unique = true, length = 500)
//  private String token;
//
//  @Column(nullable = false)
//  private LocalDateTime expiresAt;
//
//  @Column(nullable = false)
//  private Boolean revoked = false;
//
//  @Column(nullable = false)
//  private Boolean expired = false;
//
//  @Column(length = 100)
//  private String device;
//
//  @Column(length = 45)
//  private String ipAddress;
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
