package com.benkih.estore.category.repository;

import com.benkih.estore.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  Category findByName(String name);

  boolean existsByName(String name);

  Optional<Category> findBySlug(String slug);
}
