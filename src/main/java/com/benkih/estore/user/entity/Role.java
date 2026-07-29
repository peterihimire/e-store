package com.benkih.estore.user.entity;

import com.benkih.estore.permission.entity.Permission;
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
        @Index(name = "idx_roles_name", columnList = "name")
    }
)
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private String slug;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private boolean systemRole = false;

  @Column(nullable = false)
  private boolean active = true;

  private String createdBy;
  private String updatedBy;

  @Column(nullable = false,updatable = false)
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @ManyToMany(mappedBy = "roles")
  private Collection<User> users = new HashSet<>(); //using collection here allows the user to switch between Set and ArrayList

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "role_permissions",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id")
  )
  private Set<Permission> permissions = new HashSet<>();

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
