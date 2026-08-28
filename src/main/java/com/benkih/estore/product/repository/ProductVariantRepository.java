package com.benkih.estore.product.repository;

import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
  Optional<ProductVariant> findBySlug(String slug);

  Optional<ProductVariant> findBySkuAndBusinessId(
      String sku,
      Long businessId
  );

  boolean existsBySkuAndBusinessId(
      String sku,
      Long businessId
  );
}
