package com.benkih.estore.permission.repository;

import com.benkih.estore.permission.entity.Permission;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
  Optional<Permission> findByName(String name);

  Optional<Permission> findBySlug(String slug);

  boolean existsByName(String name);

  List<Permission> findAllBySlugIn(Set<String> permissionSlugs);
}
