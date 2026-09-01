package com.benkih.estore.checkout.entity;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.PromotionType;
import com.benkih.estore.common.exceptions.BadRequestException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "promotions",
    indexes = {
        @Index(
            name = "idx_promotions_code",
            columnList = "code"
        ),
        @Index(
            name = "idx_promotions_active_dates",
            columnList = "active, starts_at, expires_at"
        )
    })
@Getter
@Setter
@NoArgsConstructor
public class Promotion extends AuditableEntity {

  @Column(nullable = false, unique = true)
  private String code;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PromotionType type;

  @Column(precision = 5, scale = 2)
  private BigDecimal percentage;

  @Column(precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(name = "maximum_discount", precision = 18, scale = 2)
  private BigDecimal maximumDiscount;

  @Column(name = "minimum_order_amount", precision = 18, scale = 2)
  private BigDecimal minimumOrderAmount;

  @Column(name = "usage_limit")
  private Integer usageLimit;

  @Column(name = "usage_count", nullable = false)
  private Integer usageCount = 0;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "starts_at")
  private Instant startsAt;

  @Column(name = "expires_at")
  private Instant expiresAt;

  public void assertEligible(
      Cart cart,
      String userSlug,
      BigDecimal subtotal
  ) {

    if (!active) {
      throw new BadRequestException(
          "Promotion is inactive"
      );
    }

    if (subtotal == null ||
        subtotal.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BadRequestException(
          "Order subtotal must be greater than zero"
      );
    }

    Instant now = Instant.now();

    if (startsAt != null && now.isBefore(startsAt)) {
      throw new BadRequestException(
          "Promotion has not started"
      );
    }

    if (expiresAt != null && now.isAfter(expiresAt)) {
      throw new BadRequestException(
          "Promotion has expired"
      );
    }

    if (minimumOrderAmount != null &&
        subtotal.compareTo(minimumOrderAmount) < 0) {
      throw new BadRequestException(
          "Order does not meet the minimum amount for this promotion"
      );
    }

    validatePromotionConfiguration();
  }

  private void validatePromotionConfiguration() {

    if (type == null) {
      throw new BadRequestException(
          "Promotion type is not configured"
      );
    }

    switch (type) {

      case PERCENTAGE -> {
        if (percentage == null ||
            percentage.compareTo(BigDecimal.ZERO) <= 0 ||
            percentage.compareTo(new BigDecimal("100")) > 0) {
          throw new BadRequestException(
              "Invalid promotion percentage"
          );
        }
      }

      case FIXED_AMOUNT -> {
        if (amount == null ||
            amount.compareTo(BigDecimal.ZERO) <= 0) {
          throw new BadRequestException(
              "Invalid promotion amount"
          );
        }
      }
    }
  }
}

