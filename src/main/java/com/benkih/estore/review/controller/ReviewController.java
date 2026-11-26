package com.benkih.estore.review.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.review.dto.request.CreateReviewRequest;
import com.benkih.estore.review.dto.request.UpdateReviewRequest;
import com.benkih.estore.review.dto.response.ReviewResponse;
import com.benkih.estore.review.dto.response.ReviewSummaryResponse;
import com.benkih.estore.review.service.ReviewService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reviews", description = "Product reviews and ratings")
@RestController
@RequestMapping("${api.prefix}/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/product/{productSlug}")
    @SecurityRequirements(value = {})
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByProductSlug(
            @PathVariable String productSlug) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "Reviews retrieved",
                        reviewService.getReviewsByProductSlug(productSlug)));
    }

    @GetMapping("/product/{productSlug}/summary")
    @SecurityRequirements(value = {})
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> getProductRatingSummary(
            @PathVariable String productSlug) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "Rating summary retrieved",
                        reviewService.getProductRatingSummary(productSlug)));
    }

    @PostMapping("/product/{productSlug}")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable String productSlug, @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                "success",
                                "Review created",
                                reviewService.createReview(productSlug, request)));
    }

    @PutMapping("/{reviewSlug}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable String reviewSlug, @Valid @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "Review updated",
                        reviewService.updateReview(reviewSlug, request)));
    }

    @DeleteMapping("/{reviewSlug}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable String reviewSlug) {
        reviewService.deleteReview(reviewSlug);
        return ResponseEntity.ok(new ApiResponse<>("success", "Review deleted", null));
    }
}
