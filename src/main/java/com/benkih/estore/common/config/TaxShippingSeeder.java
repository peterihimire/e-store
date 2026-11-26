package com.benkih.estore.common.config;

import com.benkih.estore.checkout.entity.ShippingRate;
import com.benkih.estore.checkout.entity.TaxRule;
import com.benkih.estore.checkout.repository.ShippingRateRepository;
import com.benkih.estore.checkout.repository.TaxRuleRepository;
import com.benkih.estore.common.enums.Country;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.enums.DeliveryMethod;
import com.benkih.estore.common.enums.ShippingZone;
import com.benkih.estore.common.enums.TaxCategory;
import com.benkih.estore.product.entity.Category;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class TaxShippingSeeder {

    private static final Logger log = LoggerFactory.getLogger(TaxShippingSeeder.class);

    private final TaxRuleRepository taxRuleRepository;
    private final ShippingRateRepository shippingRateRepository;

    public void seedTaxRules() {
        log.info("Seeding tax rules...");

        createTaxRule(
                "NG-VAT-STANDARD-2026",
                Country.NIGERIA,
                "NG",
                TaxCategory.STANDARD,
                new BigDecimal("0.07500"),
                "VAT",
                Instant.parse("2026-01-01T00:00:00Z"),
                null);

        createTaxRule(
                "NG-VAT-ZERO-RATED-2026",
                Country.NIGERIA,
                "NG",
                TaxCategory.ZERO_RATED,
                new BigDecimal("0.00000"),
                "VAT",
                Instant.parse("2026-01-01T00:00:00Z"),
                null);

        createTaxRule(
                "NG-VAT-EXEMPT-2026",
                Country.NIGERIA,
                "NG",
                TaxCategory.EXEMPT,
                new BigDecimal("0.00000"),
                "VAT",
                Instant.parse("2026-01-01T00:00:00Z"),
                null);

        log.info("Finished seeding tax rules.");
    }

    private void createTaxRule(
            String code,
            Country country,
            String jurisdiction,
            TaxCategory taxCategory,
            BigDecimal rate,
            String taxType,
            Instant effectiveFrom,
            Instant effectiveTo) {
        if (taxRuleRepository.existsByCode(code)) {
            log.info("Tax rule already exists: {}", code);
            return;
        }

        TaxRule taxRule = new TaxRule();

        taxRule.setCode(code);
        taxRule.setCountry(country);
        taxRule.setJurisdiction(jurisdiction);
        taxRule.setTaxCategory(taxCategory);
        taxRule.setRate(rate);
        taxRule.setTaxType(taxType);
        taxRule.setEffectiveFrom(effectiveFrom);
        taxRule.setEffectiveTo(effectiveTo);
        taxRule.setActive(true);

        taxRuleRepository.save(taxRule);

        log.info("Created tax rule: {}", code);
    }

    private void createShippingRate(
            String code,
            String zone,
            DeliveryMethod deliveryMethod,
            Category category,
            BigDecimal minWeightKg,
            BigDecimal maxWeightKg,
            BigDecimal fee) {
        if (shippingRateRepository.existsByCode(code)) {
            log.info("Shipping rate already exists: {}", code);
            return;
        }

        ShippingRate rate = new ShippingRate();

        rate.setCode(code);
        rate.setZone(zone);
        rate.setDeliveryMethod(deliveryMethod);
        rate.setCategory(category);
        rate.setMinWeightKg(minWeightKg);
        rate.setMaxWeightKg(maxWeightKg);
        rate.setFee(fee);
        rate.setCurrency(CurrencyCode.NGN);
        rate.setEffectiveFrom(Instant.parse("2026-01-01T00:00:00Z"));
        rate.setActive(true);

        shippingRateRepository.save(rate);

        log.info("Created shipping rate: {}", code);
    }

    public void seedShippingRates() {

        log.info("Seeding shipping rates...");

        createShippingRate(
                "LAGOS-STANDARD",
                ShippingZone.LAGOS.code(),
                DeliveryMethod.STANDARD,
                null,
                null,
                null,
                new BigDecimal("2000"));

        createShippingRate(
                "ABUJA-STANDARD",
                ShippingZone.ABUJA.code(),
                DeliveryMethod.STANDARD,
                null,
                null,
                null,
                new BigDecimal("3000"));

        createShippingRate(
                "SOUTH-WEST-STANDARD",
                ShippingZone.SOUTH_WEST.code(),
                DeliveryMethod.STANDARD,
                null,
                null,
                null,
                new BigDecimal("3000"));

        createShippingRate(
                "SOUTH-EAST-STANDARD",
                ShippingZone.SOUTH_EAST.code(),
                DeliveryMethod.STANDARD,
                null,
                null,
                null,
                new BigDecimal("3500"));

        createShippingRate(
                "SOUTH-SOUTH-STANDARD",
                ShippingZone.SOUTH_SOUTH.code(),
                DeliveryMethod.STANDARD,
                null,
                null,
                null,
                new BigDecimal("3500"));

        createShippingRate(
                "NORTH-CENTRAL-STANDARD",
                ShippingZone.NORTH_CENTRAL.code(),
                DeliveryMethod.STANDARD,
                null,
                null,
                null,
                new BigDecimal("4000"));

        createShippingRate(
                "NORTH-EAST-STANDARD",
                ShippingZone.NORTH_EAST.code(),
                DeliveryMethod.STANDARD,
                null,
                null,
                null,
                new BigDecimal("5000"));

        createShippingRate(
                "NORTH-WEST-STANDARD",
                ShippingZone.NORTH_WEST.code(),
                DeliveryMethod.STANDARD,
                null,
                null,
                null,
                new BigDecimal("5000"));

        log.info("Finished seeding shipping rates.");
    }
}
