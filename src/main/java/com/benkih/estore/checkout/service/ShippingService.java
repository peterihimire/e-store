package com.benkih.estore.checkout.service;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.user.entity.Address;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;



@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService implements  IShippingService{
  private static final BigDecimal DEFAULT_SHIPPING_FEE = new BigDecimal("2500.00");

  public BigDecimal calculateShipping(Cart cart, Address shippingAddress) {

    if (shippingAddress == null) {
      throw new BadRequestException("Shipping address is required");
    }

    if (cart == null || cart.getItems().isEmpty()) {
      return BigDecimal.ZERO;
    }

    return DEFAULT_SHIPPING_FEE;
  }
}
