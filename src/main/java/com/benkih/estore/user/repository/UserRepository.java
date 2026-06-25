package com.benkih.estore.user.repository;


import com.benkih.estore.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findBySlug(String slug);

  boolean existsByEmail(String email);
}
