package com.benkih.estore.user.repository;


import com.benkih.estore.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  @EntityGraph(attributePaths = {
      "roles",
      "roles.permissions",
      "businessMember",
      "businessMember.roles",
      "businessMember.roles.permissions",
      "businessMember.departments"})
  Optional<User> findBySlug(String slug);

  boolean existsByEmail(String email);

  @EntityGraph(attributePaths = {
      "roles",
      "roles.permissions",
      "businessMember",
      "businessMember.roles",
      "businessMember.roles.permissions",
      "businessMember.departments"})
  Optional<User> findByEmail(String email);
}
