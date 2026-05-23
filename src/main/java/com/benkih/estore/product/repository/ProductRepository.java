package com.benkih.estore.product.repository;

import com.benkih.estore.product.dto.response.ProductsResponseDto;
import com.benkih.estore.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
  Optional<Product> findBySlug(String slug);

  List<Product> findByCategoryName(String category);

  List<Product> findByBrand(String brand);

  List<Product> findByCategoryNameAndBrand(String category, String brand);

  List<Product> findByName(String name);

  List<Product> findByBrandAndName(String brand, String name);

  Long countByBrandAndName(String brand, String name);
}
