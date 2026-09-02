package com.benkih.estore.product.repository;

import com.benkih.estore.product.entity.Brand;
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
  @EntityGraph(attributePaths = {
      "category",
      "brand"
  })
  Optional<Product> findBySlug(String slug);

  @EntityGraph(attributePaths = {"category","brand"})
  Optional<Product> findBySlugAndBusinessId( String slug, Long businessId);

  @EntityGraph(attributePaths = {"category","brand"})
  List<Product> findByCategoryName(String category);

  @EntityGraph(attributePaths = {"category","brand"})
  List<Product> findByBrandSlug(String brand);

  @EntityGraph(attributePaths = {"category","brand"})
  List<Product> findByCategoryNameAndBrandSlug(String category, String brand);

  @EntityGraph(attributePaths = { "category","brand"})
  List<Product> findByName(String name);

  @EntityGraph(attributePaths = {"category","brand"})
  Page<Product> findByBrandSlugAndName(String brand, String name,
                                   Pageable pageable);

  Long countByBrandSlugAndName(String brand, String name);

  @EntityGraph(attributePaths = {"category", "brand"})
  Page<Product> findAll(Pageable pageable);

  boolean existsByNameAndBrandAndBusinessId(String name, Brand brand, Long businessId);

  @EntityGraph(attributePaths = {"category", "brand"})
  Page<Product> findByBusinessSlug(String businessSlug, Pageable pageable);

  boolean existsByBusinessSlugAndName(String businessSlug, String name);
}
