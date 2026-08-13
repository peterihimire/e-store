package com.benkih.estore.product.repository;

import com.benkih.estore.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  Category findByName(String name);

  boolean existsByName(String name);

  Optional<Category> findBySlug(String slug);

  Optional<Category> findByBusinessIdAndSlug(String businessId, String slug);

  boolean existsByBusinessIdAndCategoryId(
      Long businessId,
      Long categoryId
  );
}
