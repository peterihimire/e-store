package com.benkih.estore.audit.entity;

import com.benkih.estore.common.entity.BaseEntity;
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
public class ApiLog extends BaseEntity {

  private String userSlug;

  private String userEmail;

  @Column(nullable = false)
  private String method;

  @Column(nullable = false)
  private String endpoint;

  private Integer statusCode;

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

}