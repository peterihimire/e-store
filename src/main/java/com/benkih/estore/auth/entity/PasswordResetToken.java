package com.benkih.estore.auth.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "password_reset_tokens",
        indexes = {
            @Index(name = "idx_password_reset_user", columnList = "user_id"),
            @Index(name = "idx_password_reset_slug", columnList = "slug")
        })
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken extends BaseEntity {

    @Column(nullable = false, updatable = false)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public void markAsUsed() {
        this.usedAt = Instant.now();
    }
}
