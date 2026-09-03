package com.benkih.estore.checkout.service;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.cart.entity.CartItem;
import com.benkih.estore.checkout.dto.ShippingQuote;
import com.benkih.estore.checkout.entity.ShippingRate;
import com.benkih.estore.checkout.repository.ShippingRateRepository;
import com.benkih.estore.common.enums.DeliveryMethod;
import com.benkih.estore.common.enums.ShippingZone;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.product.entity.PackageDimensions;
import com.benkih.estore.product.entity.ProductVariant;
import com.benkih.estore.user.entity.Address;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;


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
  public ShippingQuote quote(
      Cart cart,
      Address address,
      DeliveryMethod deliveryMethod
  ) {
    if (address == null) {
      throw new BadRequestException("Shipping address is required");
    }

    if (deliveryMethod == null) {
      throw new BadRequestException("Delivery method is required");
    }

    if (cart == null || cart.getItems().isEmpty()) {
      return new ShippingQuote(
          BigDecimal.ZERO,
          deliveryMethod,
          null,
          "EMPTY_CART"
      );
    }

    String zone = ShippingZone.from(address).code();

    log.info("Shipping zone: {}", zone);

    BigDecimal cartWeightKg = BigDecimal.ZERO;
    boolean hasUnknownWeight = false;

    for (CartItem item : cart.getItems()) {

      ProductVariant variant = item.getVariant();

      PackageDimensions dimensions =
          variant.getPackageDimensions();

      if (dimensions == null || dimensions.getWeightKg() == null) {
        hasUnknownWeight = true;
        continue;
      }

      BigDecimal itemWeight = dimensions.getWeightKg()
          .multiply(BigDecimal.valueOf(item.getQuantity()));

      cartWeightKg = cartWeightKg.add(itemWeight);
    }

    log.info(
        "Cart weight: {} kg, hasUnknownWeight: {}",
        cartWeightKg,
        hasUnknownWeight
    );

    Instant now = Instant.now();

    ShippingRate rate;

    if (hasUnknownWeight) {

      log.info(
          "Cart contains items without package weight. Using fallback shipping rate."
      );

      rate = shippingRateRepository
          .findFallbackRate(
              zone,
              deliveryMethod,
              now
          )
          .orElseThrow(() -> new BadRequestException(
              "Delivery is not available for this address and delivery method"
          ));

    } else {

      rate = shippingRateRepository
          .findApplicableRate(
              zone,
              deliveryMethod,
              cartWeightKg,
              now
          )
          .orElseThrow(() -> new BadRequestException(
              "Delivery is not available for this address and delivery method"
          ));
    }

    BigDecimal subtotal = cart.getItems().stream()
        .map(item -> item.getUnitPrice()
            .multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal amount =
        rate.getFreeShippingThreshold() != null
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
