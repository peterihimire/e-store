package com.benkih.estore.review.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "reviews",
    uniqueConstraints = { // This unique constraint is to enforce  one review per user per product at the database level
        @UniqueConstraint(
            name = "uk_review_user_product",
            columnNames = {"user_id", "product_id"}
        )
    },
    indexes = {
        @Index(
            name = "idx_reviews_business_id",
            columnList = "business_id"
        ),
        @Index(
            name = "idx_reviews_business_product",
            columnList = "business_id, product_id"
        )
    }
)
public class Review extends AuditableEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

  @Column(nullable = false)
  private Integer rating;

  @Column(length = 1000)
  private String comment;

  @Column(nullable = false)
  private Boolean verifiedPurchase = false;

}