package com.benkih.estore.permission.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.user.entity.Role;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Table(
    name = "permissions",
    indexes = {
        @Index(name = "idx_permissions_name", columnList = "name"),
        @Index(name = "idx_permissions_resource_action", columnList = "resource, action")
    }
)
public class Permission extends BaseEntity {

  @Column(nullable=false, unique=true)
  private String name;

  private String description;

  @Column(nullable = false)
  private String resource;

  @Column(nullable = false)
  private String action;

  private String createdBy;
  private String updatedBy;

  @ManyToMany(mappedBy = "permissions")
  private Set<Role> roles = new HashSet<>();

}