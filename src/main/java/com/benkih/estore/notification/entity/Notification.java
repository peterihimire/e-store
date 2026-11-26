package com.benkih.estore.notification.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.notification.enums.NotificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "notifications",
        indexes = {
            @Index(name = "uk_notification_event_key", columnList = "event_key", unique = true),
            @Index(name = "idx_notification_status", columnList = "status"),
            @Index(name = "idx_notification_recipient", columnList = "recipient"),
            @Index(name = "idx_notification_due", columnList = "status, next_attempt_at")
        })
public class Notification extends BaseEntity {

    @Column(name = "event_key", nullable = false, updatable = false, length = 255)
    private String eventKey;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(nullable = false, length = 20)
    private String channel = "EMAIL";

    @Column(nullable = false)
    private String recipient;

    @Column(length = 255)
    private String template;

    @Column(length = 500)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    /** When the dispatch service or retry worker may next pick this notification up. */
    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    /**
     * Serialized EmailRequest, including attachment bytes, so retries rebuild the original email.
     */
    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    /** Optimistic lock used to claim a notification so two workers cannot send it twice. */
    @Version
    @Column(nullable = false)
    private Long version;
}
