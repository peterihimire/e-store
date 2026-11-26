package com.benkih.estore.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.enums.OrderStatus;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.common.exceptions.ForbiddenException;
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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private CurrentUserService currentUserService;

    @InjectMocks private ReviewService reviewService;

    private User buyer;
    private Product product;

    @BeforeEach
    void setUp() {
        Business business = new Business();
        business.setId(7L);

        buyer = new User();
        buyer.setSlug("buyer-slug");
        buyer.setFirstName("Ada");
        buyer.setLastName("Lovelace");

        product = new Product();
        product.setSlug("product-slug");
        product.setName("Desk Lamp");
        product.setBusiness(business);
    }

    @Test
    void createReviewMarksVerifiedPurchaseFromDeliveredOrder() {
        when(currentUserService.getCurrentUser()).thenReturn(buyer);
        when(productRepository.findBySlug("product-slug")).thenReturn(Optional.of(product));
        when(reviewRepository.existsByUserSlugAndProductSlug("buyer-slug", "product-slug"))
                .thenReturn(false);
        when(orderRepository.existsByUserSlugAndOrderItemsProductSlugAndOrderStatus(
                        "buyer-slug", "product-slug", OrderStatus.DELIVERED))
                .thenReturn(true);
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(
                        invocation -> {
                            Review saved = invocation.getArgument(0);
                            saved.setSlug("review-slug");
                            saved.setCreatedAt(Instant.now());
                            return saved;
                        });

        ReviewResponse response =
                reviewService.createReview("product-slug", new CreateReviewRequest(5, "Excellent"));

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        Review persisted = captor.getValue();
        assertEquals(buyer, persisted.getUser());
        assertEquals(product, persisted.getProduct());
        assertEquals(product.getBusiness(), persisted.getBusiness());
        assertEquals(5, persisted.getRating());
        assertTrue(persisted.getVerifiedPurchase());
        assertTrue(response.isVerifiedPurchase());
        assertEquals("Ada Lovelace", response.getReviewerName());
        assertEquals("Desk Lamp", response.getProductName());
    }

    @Test
    void createReviewRejectsDuplicateReview() {
        when(currentUserService.getCurrentUser()).thenReturn(buyer);
        when(productRepository.findBySlug("product-slug")).thenReturn(Optional.of(product));
        when(reviewRepository.existsByUserSlugAndProductSlug("buyer-slug", "product-slug"))
                .thenReturn(true);

        assertThrows(
                AlreadyExistsException.class,
                () -> reviewService.createReview("product-slug", new CreateReviewRequest(4, null)));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReviewRejectsRatingOutsideRange() {
        assertThrows(
                BadRequestException.class,
                () -> reviewService.createReview("product-slug", new CreateReviewRequest(6, null)));

        verify(currentUserService, never()).getCurrentUser();
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void updateReviewRejectsNonOwner() {
        Review review = review("review-slug", buyer, 3);

        when(reviewRepository.findBySlug("review-slug")).thenReturn(Optional.of(review));
        when(currentUserService.getCurrentUserSlug()).thenReturn("another-user");

        assertThrows(
                ForbiddenException.class,
                () ->
                        reviewService.updateReview(
                                "review-slug", new UpdateReviewRequest(5, "Nice")));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void deleteReviewRejectsNonOwner() {
        Review review = review("review-slug", buyer, 3);

        when(reviewRepository.findBySlug("review-slug")).thenReturn(Optional.of(review));
        when(currentUserService.getCurrentUserSlug()).thenReturn("another-user");

        assertThrows(ForbiddenException.class, () -> reviewService.deleteReview("review-slug"));

        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    void ratingSummaryUsesStoredRatings() {
        when(productRepository.findBySlug("product-slug")).thenReturn(Optional.of(product));
        when(reviewRepository.countByProductSlug("product-slug")).thenReturn(3L);
        when(reviewRepository.findAverageRatingByProductSlug("product-slug"))
                .thenReturn(Optional.of(4.3333));

        ReviewSummaryResponse summary = reviewService.getProductRatingSummary("product-slug");

        assertEquals(3L, summary.getTotalReviews());
        assertEquals(new BigDecimal("4.33"), summary.getAverageRating());
    }

    @Test
    void reviewsAreReturnedNewestFirstWithMappedFields() {
        Review review = review("review-slug", buyer, 4);
        review.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));

        when(productRepository.findBySlug("product-slug")).thenReturn(Optional.of(product));
        when(reviewRepository.findByProductSlugOrderByCreatedAtDesc("product-slug"))
                .thenReturn(List.of(review));

        List<ReviewResponse> reviews = reviewService.getReviewsByProductSlug("product-slug");

        assertEquals(1, reviews.size());
        assertEquals("review-slug", reviews.get(0).getSlug());
        assertEquals(4, reviews.get(0).getRating());
        assertFalse(reviews.get(0).isVerifiedPurchase());
    }

    private Review review(String slug, User user, int rating) {
        Review review = new Review();
        review.setSlug(slug);
        review.setUser(user);
        review.setProduct(product);
        review.setBusiness(product.getBusiness());
        review.setRating(rating);
        return review;
    }
}
