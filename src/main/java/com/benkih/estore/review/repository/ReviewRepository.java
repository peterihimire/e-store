package com.benkih.estore.review.repository;

import com.benkih.estore.review.entity.Review;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findBySlug(String slug);

    boolean existsByUserSlugAndProductSlug(String userSlug, String productSlug);

    List<Review> findByProductSlugOrderByCreatedAtDesc(String productSlug);

    @Query("select avg(r.rating) from Review r where r.product.slug = :productSlug")
    Optional<Double> findAverageRatingByProductSlug(@Param("productSlug") String productSlug);

    long countByProductSlug(String productSlug);
}
