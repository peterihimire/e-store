package com.benkih.estore.review.service;

import com.benkih.estore.common.enums.OrderStatus;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.common.exceptions.ForbiddenException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.order.repository.OrderRepository;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.repository.ProductRepository;
import com.benkih.estore.review.dto.request.CreateReviewRequest;
import com.benkih.estore.review.dto.request.UpdateReviewRequest;
import com.benkih.estore.review.dto.response.ReviewResponse;
import com.benkih.estore.review.dto.response.ReviewSummaryResponse;
import com.benkih.estore.review.entity.Review;
import com.benkih.estore.review.repository.ReviewRepository;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.user.entity.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public ReviewResponse createReview(String productSlug, CreateReviewRequest request) {
        validateRating(request.getRating());

        User user = currentUserService.getCurrentUser();
        Product product = findProduct(productSlug);

        if (reviewRepository.existsByUserSlugAndProductSlug(user.getSlug(), productSlug)) {
            throw new AlreadyExistsException("You have already reviewed this product");
        }

        boolean verifiedPurchase =
                orderRepository.existsByUserSlugAndOrderItemsProductSlugAndOrderStatus(
                        user.getSlug(), productSlug, OrderStatus.DELIVERED);

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setBusiness(product.getBusiness());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setVerifiedPurchase(verifiedPurchase);

        return toResponse(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProductSlug(String productSlug) {
        findProduct(productSlug);
        return reviewRepository.findByProductSlugOrderByCreatedAtDesc(productSlug).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getProductRatingSummary(String productSlug) {
        findProduct(productSlug);

        long totalReviews = reviewRepository.countByProductSlug(productSlug);
        BigDecimal averageRating =
                reviewRepository
                        .findAverageRatingByProductSlug(productSlug)
                        .map(value -> BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP))
                        .orElse(BigDecimal.ZERO);

        return ReviewSummaryResponse.builder()
                .totalReviews(totalReviews)
                .averageRating(averageRating)
                .build();
    }

    @Transactional
    public ReviewResponse updateReview(String reviewSlug, UpdateReviewRequest request) {
        validateRating(request.getRating());

        Review review = findReview(reviewSlug);
        requireOwner(review);

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(String reviewSlug) {
        Review review = findReview(reviewSlug);
        requireOwner(review);
        reviewRepository.delete(review);
    }

    private Product findProduct(String productSlug) {
        return productRepository
                .findBySlug(productSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private Review findReview(String reviewSlug) {
        return reviewRepository
                .findBySlug(reviewSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
    }

    private void requireOwner(Review review) {
        String currentUserSlug = currentUserService.getCurrentUserSlug();
        if (!review.getUser().getSlug().equals(currentUserSlug)) {
            throw new ForbiddenException("You can only modify your own review");
        }
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }
    }

    private ReviewResponse toResponse(Review review) {
        User user = review.getUser();
        Product product = review.getProduct();

        return ReviewResponse.builder()
                .slug(review.getSlug())
                .rating(review.getRating())
                .comment(review.getComment())
                .verifiedPurchase(Boolean.TRUE.equals(review.getVerifiedPurchase()))
                .productSlug(product.getSlug())
                .productName(product.getName())
                .reviewerName(user.getFirstName() + " " + user.getLastName())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
