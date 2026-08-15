package com.benkih.estore.product.repository;

import com.benkih.estore.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  Category findByNameAndBusinessId(String name, Long businessId);

  boolean existsByName(String name);

  Optional<Category> findBySlug(String slug);

  Optional<Category> findByBusinessSlugAndSlug(String businessSlug,
                                               String slug);

  boolean existsByBusinessSlugAndSlug(
      String businessSlug,
      String slug
  );
}
