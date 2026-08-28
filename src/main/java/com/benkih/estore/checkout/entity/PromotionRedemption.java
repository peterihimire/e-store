package com.benkih.estore.checkout.entity;

import com.benkih.estore.order.entity.Order;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "promotion_redemptions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_promotion_order",
            columnNames = {"promotion_id", "order_id"}
        )
    }
)
public class PromotionRedemption {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "promotion_id", nullable = false)
  private Promotion promotion;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal discountAmount;

  @Column(nullable = false, updatable = false)
  private Instant redeemedAt;
}
