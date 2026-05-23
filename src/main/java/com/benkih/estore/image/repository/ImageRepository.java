package com.benkih.estore.image.repository;

import com.benkih.estore.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
  Optional<Image> findBySlug(String slug);

}
