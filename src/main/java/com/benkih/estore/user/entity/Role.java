package com.benkih.estore.user.entity;

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
@Table(name = "roles")
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private String slug;

  @Column(nullable = false, unique = true)
  private String name;

  private String createdBy;
  private String updatedBy;

  @Column(nullable = false,updatable = false)
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @ManyToMany(mappedBy = "roles")
  private Collection<User> users = new HashSet<>(); //using collection here allows the user to switch between Set and ArrayList

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
