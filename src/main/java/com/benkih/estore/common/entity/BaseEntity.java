package com.benkih.estore.common.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long id;

  @Column(nullable = false, unique = true, updatable = false)
  protected String slug;

  @Column(nullable = false, updatable = false)
  protected LocalDateTime createdAt;
  protected LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    if (slug == null) {
      slug = UUID.randomUUID().toString();
    }

    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}