package com.benkih.estore.department.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Table(
    name = "departments",
    indexes = {
        @Index(name = "idx_departments_name", columnList = "name")
    }
)
public class Department  extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String name;

  private String description;

  @ManyToMany(mappedBy = "departments")
  private Set<User> users = new HashSet<>();

  private String createdBy;
  private String updatedBy;
}