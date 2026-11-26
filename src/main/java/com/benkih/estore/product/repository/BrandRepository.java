package com.benkih.estore.product.repository;

import com.benkih.estore.product.entity.Brand;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findBySlug(String slug);

    boolean existsByNormalizedName(String normalizedName);
}
