package com.benkih.estore.product.repository;


import com.benkih.estore.product.entity.Category;
import com.benkih.estore.product.entity.CategoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryAttributeRepository
    extends JpaRepository<CategoryAttribute, Long> {

  Optional<CategoryAttribute> findBySlug(String slug);

  Optional<CategoryAttribute> findByCategoryAndName(
      Category category,
      String name
  );

  Optional<CategoryAttribute> findByCategoryAndSlug(
      Category category,
      String slug
  );

  List<CategoryAttribute>
  findByCategoryAndActiveTrueAndVariantAttributeTrueAndRequiredTrue(
      Category category
  );

  List<CategoryAttribute> findByCategory(
      Category category
  );

  List<CategoryAttribute> findByCategoryId(
      Long categoryId
  );

  boolean existsByCategoryAndName(
      Category category,
      String name
  );

  boolean existsByCategoryIdAndName(
      Long categoryId,
      String name
  );
}
