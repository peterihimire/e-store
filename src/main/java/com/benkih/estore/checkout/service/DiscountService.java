package com.benkih.estore.checkout.service;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.checkout.dto.DiscountQuote;
import java.math.BigDecimal;

public interface DiscountService {
    BigDecimal calculateDiscount(Cart cart, BigDecimal subtotal);

    DiscountQuote quote(Cart cart, BigDecimal subtotal, String couponCode, String userSlug);
}
