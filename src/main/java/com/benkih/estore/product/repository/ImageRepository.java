package com.benkih.estore.product.repository;

import com.benkih.estore.product.entity.Category;
import com.benkih.estore.product.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
  Optional<Image> findBySlug(String slug);
  Optional<Image> findByBusinessSlugAndSlug(String businessSlug, String slug);

}
