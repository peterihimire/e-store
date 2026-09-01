package com.benkih.estore.product.repository;

import com.benkih.estore.product.entity.Brand;
import com.benkih.estore.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
  Optional<Brand> findBySlug(String slug);
}
