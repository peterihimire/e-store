package com.benkih.estore.checkout.service;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.checkout.dto.DiscountQuote;
import com.benkih.estore.checkout.entity.Promotion;
import com.benkih.estore.checkout.repository.PromotionRepository;
import com.benkih.estore.common.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import static com.benkih.estore.common.enums.PromotionType.FIXED_AMOUNT;
import static com.benkih.estore.common.enums.PromotionType.PERCENTAGE;


@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountService implements IDiscountService {
  private final PromotionRepository promotionRepository;

  public BigDecimal calculateDiscount(Cart cart, BigDecimal subtotal) {

    if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    // No discount for now
    return BigDecimal.ZERO;
  }

  @Transactional
  public DiscountQuote quote(
      Cart cart, BigDecimal subtotal, String couponCode, String userSlug
  ) {
    if (subtotal == null || subtotal.signum() <= 0 || couponCode == null) {
      return new DiscountQuote(BigDecimal.ZERO, null, null);
    }

    Promotion promotion = promotionRepository.findActiveForUpdate(couponCode, Instant.now())
        .orElseThrow(() -> new BadRequestException("Invalid or expired coupon"));

    promotion.assertEligible(cart, userSlug, subtotal);

    BigDecimal amount = switch (promotion.getType()) {
      case PERCENTAGE -> subtotal
          .multiply(promotion.getPercentage().movePointLeft(2))
          .min(promotion.getMaximumDiscount());
      case FIXED_AMOUNT -> promotion.getAmount();
    };

    amount = amount.min(subtotal).setScale(2, RoundingMode.HALF_UP);

    return new DiscountQuote(amount, promotion.getCode(), promotion.getName());
  }
}
