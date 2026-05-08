package com.benkih.estore.category.repository;

import com.benkih.estore.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {

  Category findByName(String name);

  boolean existsByName(String name);
}
