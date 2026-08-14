package com.benkih.estore.product.repository;

import com.benkih.estore.product.entity.Category;
import com.benkih.estore.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
  @EntityGraph(attributePaths = {"images", "category", "inventory"})
  Optional<Product> findBySlug(String slug);

  @EntityGraph(attributePaths = {"images", "category", "inventory"})
  Optional<Product> findByBusinessSlugAndSlug(String businessSlug, String slug);

  @EntityGraph(attributePaths = {"images", "category", "inventory"})
  List<Product> findByCategoryName(String category);

  @EntityGraph(attributePaths = {"images", "category", "inventory"})
  List<Product> findByBrand(String brand);

  @EntityGraph(attributePaths = {"images", "category", "inventory"})
  List<Product> findByCategoryNameAndBrand(String category, String brand);

  @EntityGraph(attributePaths = {"images", "category", "inventory"})
  List<Product> findByName(String name);

  @EntityGraph(attributePaths = {"images", "category", "inventory"})
  Page<Product> findByBrandAndName(String brand, String name,
                                   Pageable pageable);

  Long countByBrandAndName(String brand, String name);

  @EntityGraph(attributePaths = {"images", "category", "inventory"})
  List<Product> findAll();

  boolean existsByNameAndBrand(String name, String brand);

  Page<Product> findByBusinessSlug(String businessSlug, Pageable pageable);

  boolean existsByBusinessSlugAndName(String businessSlug, String name);
}
