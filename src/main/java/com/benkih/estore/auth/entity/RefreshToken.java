package com.benkih.estore.auth.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.RevocationReason;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
        @Index(name = "idx_refresh_user", columnList = "user_id"),
        @Index(name = "idx_refresh_expires", columnList = "expiresAt")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken extends BaseEntity {

  @Column(nullable = false)
  private String tokenHash;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private boolean revoked = false;

  private Instant lastUsedAt;

  private Instant revokedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "revoked_reason")
  private RevocationReason revokedReason;

  @Column(length = 255)
  private String device;

  private String ipAddress;

  public boolean isExpired() {
    return !expiresAt.isAfter(Instant.now());
  }

}
