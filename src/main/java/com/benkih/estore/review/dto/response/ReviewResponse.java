package com.benkih.estore.review.dto.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {
    private String slug;
    private Integer rating;
    private String comment;
    private boolean verifiedPurchase;
    private String productSlug;
    private String productName;
    private String reviewerName;
    private Instant createdAt;
    private Instant updatedAt;
}
