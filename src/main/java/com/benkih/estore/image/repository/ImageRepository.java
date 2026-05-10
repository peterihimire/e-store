package com.benkih.estore.image.repository;

import com.benkih.estore.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, String> {

}
