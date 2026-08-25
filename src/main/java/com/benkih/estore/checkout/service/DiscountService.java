package com.benkih.estore.checkout.service;

import com.benkih.estore.cart.entity.Cart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;



@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountService implements IDiscountService {

  public BigDecimal calculateDiscount(Cart cart, BigDecimal subtotal) {

    if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    // No discount for now
    return BigDecimal.ZERO;
  }
}
