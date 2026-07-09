package com.benkih.estore.audit.entity;

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
@Table(name = "audit_logs")
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private String slug;

  /**
   * User that performed the action.
   * Null for anonymous requests.
   */
  private String userSlug;

  private String userEmail;

  /**
   * Shared across the user's journey.
   * Obtained from X-Trace-Id.
   */
  @Column(length = 100)
  private String traceId;

  /**
   * Login session identifier.
   * Null until the user authenticates.
   */
  @Column(length = 100)
  private String sessionId;

  /**
   * CREATE_PRODUCT
   * UPDATE_ORDER
   * LOGIN
   * PAYMENT_SUCCESS
   */
  @Column(nullable = false)
  private String action;

  /**
   * PRODUCT
   * ORDER
   * USER
   * PAYMENT
   * ADDRESS
   */
  @Column(nullable = false)
  private String entityType;

  /**
   * Database ID of the affected entity.
   */
  private Long entityId;

  /**
   * Slug of the affected entity.
   */
  private String entitySlug;

  /**
   * success / fail
   */
  @Column(nullable = false)
  private String status;

  /**
   * CREATE_ORDER success
   * LOGIN failed
   */
  private String outcome;

  /**
   * Previous state (JSON)
   */
  @Lob
  private String oldValue;

  /**
   * New state (JSON)
   */
  @Lob
  private String newValue;

  /**
   * Request payload (JSON)
   */
  @Lob
  private String request;

  /**
   * Response payload (JSON)
   */
  @Lob
  private String response;

  /**
   * Extra information.
   */
  @Lob
  private String description;

  /**
   * Metadata JSON.
   * method, platform, origin,
   * referer, contentType, etc.
   */
  @Lob
  private String metadata;

  @Column(length = 45)
  private String requestIp;

  @Column(length = 1000)
  private String userAgent;

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