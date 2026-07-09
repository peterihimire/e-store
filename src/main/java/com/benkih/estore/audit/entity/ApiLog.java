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
@Table(name = "api_logs")
public class ApiLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private String slug;

  private String userSlug;

  private String userEmail;

  @Column(nullable = false)
  private String method;

  @Column(nullable = false)
  private String endpoint;

  private Integer statusCode;

  /**
   * Duration in milliseconds
   */
  private Long durationMs;

  private String requestIp;

  @Column(length = 1000)
  private String userAgent;

  @Column(length = 100)
  private String traceId;

  @Column(columnDefinition = "TEXT")
  private String requestBody;

  @Column(columnDefinition = "TEXT")
  private String responseBody;

  @Column(columnDefinition = "TEXT")
  private String exceptionMessage;

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