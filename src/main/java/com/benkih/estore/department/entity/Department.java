package com.benkih.estore.department.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(
    name = "departments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_department_business_name",
            columnNames = {"business_id", "name"}
        )
    },
    indexes = {
        @Index(name = "idx_departments_business_id", columnList = "business_id"),
        @Index(name = "idx_departments_business_name", columnList = "business_id, name")
    }
)
public class Department  extends BaseEntity {

  @Column(nullable = false)
  private String name;

  private String description;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

  @ManyToMany(mappedBy = "departments")
  private Set<User> users = new HashSet<>();

  private String createdBy;
  private String updatedBy;
}