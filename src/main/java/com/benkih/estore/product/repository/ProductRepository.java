package com.benkih.estore.product.repository;

import com.benkih.estore.product.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
  @EntityGraph(attributePaths = {"images", "category"})
  Optional<Product> findBySlug(String slug);

  @EntityGraph(attributePaths = {"images", "category"})
  List<Product> findByCategoryName(String category);

  @EntityGraph(attributePaths = {"images", "category"})
  List<Product> findByBrand(String brand);

  @EntityGraph(attributePaths = {"images", "category"})
  List<Product> findByCategoryNameAndBrand(String category, String brand);

  @EntityGraph(attributePaths = {"images", "category"})
  List<Product> findByName(String name);

  @EntityGraph(attributePaths = {"images", "category"})
  List<Product> findByBrandAndName(String brand, String name);

  Long countByBrandAndName(String brand, String name);

//  @EntityGraph(attributePaths = {"images", "category"})
//  List<Product> findAll();

  boolean existsByNameAndBrand(String name, String brand);
}
