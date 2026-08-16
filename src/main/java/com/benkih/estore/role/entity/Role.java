package com.benkih.estore.role.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.permission.entity.Permission;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor // use this when you create your own arg constructor manually
@Entity
@Table(
    name = "roles",
    indexes = {
        @Index(name = "idx_roles_slug", columnList = "slug"),
        @Index(name = "idx_roles_name", columnList = "name"),
        @Index(name = "idx_roles_business_id", columnList = "business_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_role_business_name",
            columnNames = {"business_id", "name"}
        )
    }
)
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private String slug;

  @Column(nullable = false)
  private String name;

  @Column(length = 500)
  private String description;

  @Column(nullable = false)
  private boolean systemRole = false;

  @Column(nullable = false)
  private boolean active = true;

  private String createdBy;
  private String updatedBy;

  @Column(nullable = false,updatable = false)
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /*
   * System roles are associated with users through this relationship.
   *
   * Business roles should NOT be assigned here.
   * Business roles are assigned through BusinessMember.
   */
  @ManyToMany(mappedBy = "roles")
  private Collection<User> users = new HashSet<>(); //using collection here allows the user to switch between Set and ArrayList

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "role_permissions",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id")
  )
  private Set<Permission> permissions = new HashSet<>();

  /*
   * NULL  -> system role
   * NOT NULL -> business-specific role
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "business_id")
  private Business business;

  public Role(String name) { //manual arg constructor
    this.name = name;
  }

  @PrePersist
  public void onCreate() {

    if (this.slug == null) {
      this.slug = UUID.randomUUID().toString();
    }
    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
