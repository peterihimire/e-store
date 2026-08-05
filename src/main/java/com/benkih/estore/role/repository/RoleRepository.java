package com.benkih.estore.role.repository;

import com.benkih.estore.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository  extends JpaRepository<Role, Long> {
  Optional<Role> findByName(String name);

  Optional<Role> findBySlug(String slug);

  Optional<Role> findByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCase(String name);

  List<Role> findAllBySlugIn(Collection<String> slugs);
}
