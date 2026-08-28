package com.benkih.estore.checkout.service;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.checkout.dto.ShippingQuote;
import com.benkih.estore.checkout.entity.ShippingRate;
import com.benkih.estore.checkout.repository.ShippingRateRepository;
import com.benkih.estore.common.enums.DeliveryMethod;
import com.benkih.estore.common.enums.ShippingZone;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.user.entity.Address;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService implements  IShippingService{
  private static final BigDecimal DEFAULT_SHIPPING_FEE = new BigDecimal("2500.00");
  private final ShippingRateRepository shippingRateRepository;

  @Override
  public BigDecimal calculateShipping(Cart cart, Address shippingAddress) {

    if (shippingAddress == null) {
      throw new BadRequestException("Shipping address is required");
    }

    if (cart == null || cart.getItems().isEmpty()) {
      return BigDecimal.ZERO;
    }

    return DEFAULT_SHIPPING_FEE;
  }


  @Override
  public ShippingQuote quote(Cart cart, Address address, DeliveryMethod deliveryMethod) {
    if (address == null) {
      throw new BadRequestException("Shipping address is required");
    }
    if (cart == null || cart.getItems().isEmpty()) {
      return new ShippingQuote(BigDecimal.ZERO, DeliveryMethod.STANDARD, null,
          "EMPTY_CART");
    }

    String zone = ShippingZone.from(address).code();
    BigDecimal cartWeightKg = cart.getItems().stream()
        .map(item -> item.getProduct().getWeightKg()
            .multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    ShippingRate rate = shippingRateRepository
        .findApplicableRate(zone, deliveryMethod, cartWeightKg)
        .orElseThrow(() -> new BadRequestException(
            "Delivery is not available for this address and delivery method"
        ));

    BigDecimal subtotal = cart.getItems().stream()
        .map(item -> item.getUnitPrice()
            .multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal amount = rate.getFreeShippingThreshold() != null
        && subtotal.compareTo(rate.getFreeShippingThreshold()) >= 0
        ? BigDecimal.ZERO
        : rate.getFee();

    return new ShippingQuote(
        amount.setScale(2, RoundingMode.HALF_UP),
        deliveryMethod,
        zone,
        rate.getCode()
    );
  }
}
