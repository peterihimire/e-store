package com.benkih.estore.business.repository;

import com.benkih.estore.business.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

  Optional<Business> findBySlug(String slug);

  Optional<Business> findByEmail(String email);

  boolean existsBySlug(String slug);

  boolean existsByEmail(String email);
}
