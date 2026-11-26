package com.benkih.estore.notification.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.notification.enums.AttemptOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "delivery_attempts",
        indexes = {
            @Index(name = "idx_delivery_attempt_notification", columnList = "notification_id"),
            @Index(name = "idx_delivery_attempt_outcome", columnList = "outcome")
        })
public class DeliveryAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AttemptOutcome outcome;

    @Column(length = 50)
    private String provider;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;
}
