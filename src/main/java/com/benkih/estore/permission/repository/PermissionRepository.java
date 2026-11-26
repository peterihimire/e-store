package com.benkih.estore.permission.repository;

import com.benkih.estore.permission.entity.Permission;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);

    Optional<Permission> findBySlug(String slug);

    boolean existsByName(String name);

    List<Permission> findAllBySlugIn(Set<String> permissionSlugs);
}
