package com.benkih.estore.checkout.service;

import com.benkih.estore.checkout.dto.TaxQuote;
import com.benkih.estore.checkout.entity.TaxRule;
import com.benkih.estore.checkout.repository.TaxRuleRepository;
import com.benkih.estore.common.enums.TaxCategory;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.order.entity.OrderItem;
import com.benkih.estore.user.entity.Address;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class TaxService implements ITaxService {
  private final TaxRuleRepository taxRuleRepository;
  private static final BigDecimal TAX_RATE = new BigDecimal("0.075");


  public BigDecimal calculate(BigDecimal taxableAmount) {
    if (taxableAmount == null || taxableAmount.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    return taxableAmount
        .multiply(TAX_RATE)
        .setScale(2, RoundingMode.HALF_UP);
  }


  @Override
  public TaxQuote quote(
      List<OrderItem> items,
      BigDecimal discount,
      Address address
  ) {

    if (items == null || items.isEmpty()) {
      return new TaxQuote(
          BigDecimal.ZERO.setScale(2),
          BigDecimal.ZERO,
          "NG",
          "VAT",
          null
      );
    }

    BigDecimal safeDiscount = discount == null
        ? BigDecimal.ZERO
        : discount.max(BigDecimal.ZERO);

    BigDecimal taxableSubtotal = items.stream()
        .filter(this::isTaxable)
        .map(this::calculateItemSubtotal)
        .reduce(
            BigDecimal.ZERO,
            BigDecimal::add
        );

    if (taxableSubtotal.signum() <= 0) {
      // Make sure every item has a valid snapshot

      items.forEach(item -> {
        item.setTaxRate(BigDecimal.ZERO);
        item.setTaxAmount(BigDecimal.ZERO);
      });

      return new TaxQuote(
          BigDecimal.ZERO.setScale(2),
          BigDecimal.ZERO,
          "NG",
          "VAT",
          null
      );
    }

    BigDecimal taxableDiscount = allocateDiscountToTaxableItems(
            items,
            safeDiscount,
            taxableSubtotal
        );

    BigDecimal taxableAmount = taxableSubtotal
        .subtract(taxableDiscount)
        .max(BigDecimal.ZERO);

    TaxRule rule = taxRuleRepository.findApplicableRule(
        "NG",
        TaxCategory.STANDARD,
        Instant.now()
    ).orElseThrow(() ->
        new BadRequestException("No applicable tax rule found")
    );

    BigDecimal tax = taxableAmount
        .multiply(rule.getRate())
        .setScale(
            2,
            RoundingMode.HALF_UP
        );

   //  Populate tax snapshot on each order item.

    for (OrderItem item : items) {
      if (!isTaxable(item)) {
        item.setTaxRate(BigDecimal.ZERO);
        item.setTaxAmount(BigDecimal.ZERO);
        continue;
      }

      BigDecimal itemSubtotal = calculateItemSubtotal(item);
      BigDecimal itemDiscount = allocateItemDiscount(
          item,
          taxableSubtotal,
          taxableDiscount
      );

      BigDecimal taxableItemAmount = itemSubtotal
          .subtract(itemDiscount)
          .max(BigDecimal.ZERO);

      BigDecimal itemTax = taxableItemAmount
          .multiply(rule.getRate())
          .setScale(2, RoundingMode.HALF_UP);

      item.setTaxRate(rule.getRate());
      item.setTaxAmount(itemTax);
      item.setDiscountAmount(itemDiscount);
    }

    return new TaxQuote(
        tax,
        rule.getRate(),
        rule.getJurisdiction(),
        rule.getTaxType(),
        rule.getCode()
    );
  }

  private BigDecimal allocateItemDiscount(
      OrderItem item,
      BigDecimal taxableSubtotal,
      BigDecimal taxableDiscount
  ) {

    BigDecimal itemSubtotal = calculateItemSubtotal(item);

    if (taxableSubtotal.signum() <= 0 ||
        taxableDiscount.signum() <= 0) {

      return BigDecimal.ZERO.setScale(2);
    }

    return taxableDiscount
        .multiply(itemSubtotal)
        .divide(
            taxableSubtotal,
            2,
            RoundingMode.HALF_UP
        )
        .min(itemSubtotal)
        .setScale(2, RoundingMode.HALF_UP);
  }


  private BigDecimal allocateDiscountToTaxableItems(
      List<OrderItem> items,
      BigDecimal discount,
      BigDecimal taxableSubtotal
  ) {

    if (discount == null ||
        discount.signum() <= 0 ||
        taxableSubtotal == null ||
        taxableSubtotal.signum() <= 0) {

      return BigDecimal.ZERO.setScale(2);
    }

    BigDecimal totalSubtotal = items.stream()
        .map(this::calculateItemSubtotal)
        .reduce(
            BigDecimal.ZERO,
            BigDecimal::add
        );

    if (totalSubtotal.signum() <= 0) {
      return BigDecimal.ZERO.setScale(2);
    }

    BigDecimal taxableDiscount = discount
        .multiply(taxableSubtotal)
        .divide(
            totalSubtotal,
            2,
            RoundingMode.HALF_UP
        );

    return taxableDiscount
        .min(taxableSubtotal)
        .setScale(
            2,
            RoundingMode.HALF_UP
        );
  }


  private BigDecimal calculateItemSubtotal(OrderItem item) {
    return item.getPrice()
        .multiply(
            BigDecimal.valueOf(item.getQuantity())
        );
  }

  private boolean isTaxable(OrderItem item) {
    return item.getTaxCategory() == TaxCategory.STANDARD;
  }
}

//  private BigDecimal allocateDiscountToTaxableItems(
//
//      List<OrderItem> items,
//
//      BigDecimal discount,
//
//      BigDecimal taxableSubtotal
//
//  ) {
//
//    if (discount == null ||
//
//        discount.signum() <= 0 ||
//
//        taxableSubtotal == null ||
//
//        taxableSubtotal.signum() <= 0) {
//
//      return BigDecimal.ZERO.setScale(2);
//
//    }
//
//    BigDecimal totalSubtotal = items.stream()
//
//        .map(this::calculateItemSubtotal)
//
//        .reduce(
//
//            BigDecimal.ZERO,
//
//            BigDecimal::add
//
//        );
//
//    if (totalSubtotal.signum() <= 0) {
//
//      return BigDecimal.ZERO.setScale(2);
//
//    }
//
//    BigDecimal taxableDiscount = discount
//
//        .multiply(taxableSubtotal)
//
//        .divide(
//
//            totalSubtotal,
//
//            2,
//
//            RoundingMode.HALF_UP
//
//        );
//
//    return taxableDiscount
//
//        .min(taxableSubtotal)
//
//        .setScale(
//
//            2,
//
//            RoundingMode.HALF_UP
//
//        );
//
//  }
//}

//  public TaxQuote quote(List<OrderItem> items, BigDecimal discount, Address address) {
//    BigDecimal taxableSubtotal = items.stream()
//        .filter(item -> item.getProduct().getTaxCategory() == TaxCategory.STANDARD)
//        .map(item -> item.getPrice()
//            .multiply(BigDecimal.valueOf(item.getQuantity())))
//        .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//    BigDecimal taxableDiscount = allocateDiscountToTaxableItems(
//        items, discount, taxableSubtotal
//    );
//
//    BigDecimal tax = taxableSubtotal.subtract(taxableDiscount)
//        .max(BigDecimal.ZERO)
//        .multiply(new BigDecimal("0.075"))
//        .setScale(2, RoundingMode.HALF_UP);
//
//    return new TaxQuote(tax, new BigDecimal("0.075"), "NG", "NG_VAT_STANDARD");
//  }


//  private BigDecimal allocateDiscountToTaxableItems(
//      List<OrderItem> items,
//      BigDecimal discount,
//      BigDecimal taxableSubtotal
//  ) {
//
//    if (discount == null ||
//        discount.compareTo(BigDecimal.ZERO) <= 0) {
//      return BigDecimal.ZERO;
//    }
//
//    if (taxableSubtotal == null ||
//        taxableSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
//      return BigDecimal.ZERO;
//    }
//
//    BigDecimal totalSubtotal = items.stream()
//        .map(this::calculateItemSubtotal)
//        .reduce(
//            BigDecimal.ZERO,
//            BigDecimal::add
//        );
//
//    if (totalSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
//      return BigDecimal.ZERO;
//    }
//
//    BigDecimal taxableDiscount = discount
//        .multiply(taxableSubtotal)
//        .divide(
//            totalSubtotal,
//            2,
//            RoundingMode.HALF_UP
//        );
//
//    return taxableDiscount
//        .min(taxableSubtotal)
//        .setScale(
//            2,
//            RoundingMode.HALF_UP
//        );
//  }

//  @Override
//  public TaxQuote quote(
//      List<OrderItem> items,
//      BigDecimal discount,
//      Address address
//  ) {
//
//    if (items == null || items.isEmpty()) {
//      return new TaxQuote(
//          BigDecimal.ZERO.setScale(2),
//          BigDecimal.ZERO,
//          "NG",
//          "VAT",
//          null
//      );
//    }
//
//    BigDecimal taxableSubtotal = items.stream()
//            .filter(item ->
//                item.getProduct()
//                    .getTaxCategory()
//                    == TaxCategory.STANDARD
//            )
//            .map(item ->
//                item.getPrice()
//                    .multiply(
//                        BigDecimal.valueOf(
//                            item.getQuantity()
//                        )
//                    )
//            )
//            .reduce(
//                BigDecimal.ZERO,
//                BigDecimal::add
//            );
//
//    if (taxableSubtotal.signum() <= 0) {
//      return new TaxQuote(
//          BigDecimal.ZERO.setScale(2),
//          BigDecimal.ZERO,
//          "NG",
//          "VAT",
//          null
//      );
//    }
//
//    BigDecimal taxableDiscount = allocateDiscountToTaxableItems(
//            items,
//            discount,
//            taxableSubtotal
//        );
//
//    BigDecimal taxableAmount = taxableSubtotal
//            .subtract(taxableDiscount)
//            .max(BigDecimal.ZERO);
//
//    TaxRule rule = taxRuleRepository.findApplicableRule(
//                "NG",
//                TaxCategory.STANDARD,
//                LocalDateTime.now()
//            )
//            .orElseThrow(() ->
//                new BadRequestException("No applicable tax rule found")
//            );
//
//    BigDecimal tax = taxableAmount.multiply(rule.getRate())
//            .setScale(
//                2,
//                RoundingMode.HALF_UP
//            );
//
//    return new TaxQuote(
//        tax,
//        rule.getRate(),
//        rule.getJurisdiction(),
//        rule.getTaxType(),
//        rule.getCode()
//    );
//  }