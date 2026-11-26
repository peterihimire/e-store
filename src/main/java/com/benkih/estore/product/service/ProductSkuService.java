package com.benkih.estore.product.service;

import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.entity.ProductVariant;
import com.benkih.estore.product.repository.SkuSequenceRepository;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSkuService {
    private final SkuSequenceRepository skuSequenceRepository;

    public String generateSku(Product product) {
        long sequence = skuSequenceRepository.nextSkuNumber();
        String brandCode = abbreviate(String.valueOf(product.getBrand()));
        String productCode = abbreviate(product.getName());

        return String.format("%s-%s-%04d", brandCode, productCode, sequence);
    }

    public String generateSku(Product product, ProductVariant variant) {
        long sequence = skuSequenceRepository.nextSkuNumber();

        String businessCode = skuToken(product.getBusiness().getName(), 4);

        String brandCode = skuToken(String.valueOf(product.getBrand().getName()), 5);

        String productCode = skuToken(product.getName(), 8);

        String variantCode = variantCode(variant);

        return String.format(
                "%s-%s-%s-%s-%06d", businessCode, brandCode, productCode, variantCode, sequence);
    }

    public String variantCode(ProductVariant variant) {
        if (variant.getAttributes() == null || variant.getAttributes().isEmpty()) {
            return "DEF";
        }

        return variant.getAttributes().stream()
                .sorted(Comparator.comparing(item -> item.getAttribute().getName()))
                .map(
                        item -> {
                            String value =
                                    item.getAttributeValue() != null
                                            ? item.getAttributeValue().getValue()
                                            : item.getCustomValue();

                            return skuToken(value, 6);
                        })
                .collect(Collectors.joining("-"));
    }

    public String skuToken(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "GEN";
        }

        String normalized = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");

        return normalized.substring(0, Math.min(normalized.length(), maxLength));
    }

    public String abbreviate(String value) {
        String cleaned = value.replaceAll("[^A-Za-z]", "").toUpperCase();

        return cleaned.substring(0, Math.min(3, cleaned.length()));
    }
}
