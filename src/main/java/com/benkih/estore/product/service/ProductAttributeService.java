package com.benkih.estore.product.service;

import com.benkih.estore.common.enums.AttributeType;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.product.dto.request.ProductAttributeRequest;
import com.benkih.estore.product.dto.request.VariantAttributeRequest;
import com.benkih.estore.product.entity.AttributeValue;
import com.benkih.estore.product.entity.Category;
import com.benkih.estore.product.entity.CategoryAttribute;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.entity.ProductAttribute;
import com.benkih.estore.product.entity.ProductVariant;
import com.benkih.estore.product.entity.ProductVariantAttribute;
import com.benkih.estore.product.repository.AttributeValueRepository;
import com.benkih.estore.product.repository.CategoryAttributeRepository;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductAttributeService {
    private final CategoryAttributeRepository categoryAttributeRepository;
    private final AttributeValueRepository attributeValueRepository;

    public String buildCombinationKey(List<ProductVariantAttribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "default";
        }

        return attributes.stream()
                .sorted(Comparator.comparing(item -> item.getAttribute().getSlug()))
                .map(
                        item -> {
                            String attributeSlug = item.getAttribute().getSlug();

                            String value =
                                    item.getAttributeValue() != null
                                            ? item.getAttributeValue().getSlug()
                                            : item.getCustomValue();

                            return attributeSlug + "=" + normalizeCombinationValue(value);
                        })
                .collect(Collectors.joining("|"));
    }

    public String normalizeCombinationValue(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("A variant attribute value is required.");
        }

        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
    }

    public void validateRequiredProductAttributes(
            Category category, Set<String> requestedAttributeSlugs) {
        List<CategoryAttribute> requiredAttributes =
                categoryAttributeRepository
                        .findByCategoryAndActiveTrueAndVariantAttributeFalseAndRequiredTrue(
                                category);

        for (CategoryAttribute attribute : requiredAttributes) {
            if (!requestedAttributeSlugs.contains(attribute.getSlug())) {
                throw new BadRequestException(
                        "Product attribute '" + attribute.getName() + "' is required.");
            }
        }
    }

    public void populateAndValidateProductAttributes(
            Product product, List<ProductAttributeRequest> requests, Category category) {
        List<ProductAttributeRequest> attributeRequests = requests == null ? List.of() : requests;

        Set<String> requestedAttributeSlugs = new HashSet<>();

        for (ProductAttributeRequest request : attributeRequests) {
            String attributeSlug = request.getAttributeSlug().trim();

            if (!requestedAttributeSlugs.add(attributeSlug)) {
                throw new BadRequestException("Duplicate product attribute: " + attributeSlug);
            }

            CategoryAttribute attribute =
                    categoryAttributeRepository
                            .findByCategoryAndSlug(category, attributeSlug)
                            .orElseThrow(
                                    () ->
                                            new BadRequestException(
                                                    "Attribute does not belong to category: "
                                                            + attributeSlug));

            if (!attribute.isActive()) {
                throw new BadRequestException("Attribute is inactive: " + attribute.getName());
            }

            if (attribute.isVariantAttribute()) {
                throw new BadRequestException(
                        "Attribute '"
                                + attribute.getName()
                                + "' must be supplied on a product variant.");
            }

            ProductAttribute productAttribute = createProductAttribute(product, attribute, request);

            product.getAttributes().add(productAttribute);
        }

        validateRequiredProductAttributes(category, requestedAttributeSlugs);
    }

    public ProductAttribute createProductAttribute(
            Product product, CategoryAttribute attribute, ProductAttributeRequest request) {
        boolean hasValueSlug =
                request.getAttributeValueSlug() != null
                        && !request.getAttributeValueSlug().isBlank();

        boolean hasCustomValue =
                request.getCustomValue() != null && !request.getCustomValue().isBlank();

        if (hasValueSlug == hasCustomValue) {
            throw new BadRequestException(
                    "Provide either attributeValueSlug or customValue for '"
                            + attribute.getName()
                            + "'.");
        }

        ProductAttribute productAttribute = new ProductAttribute();

        productAttribute.setProduct(product);
        productAttribute.setAttribute(attribute);

        if (hasValueSlug) {
            AttributeValue attributeValue =
                    attributeValueRepository
                            .findByAttributeAndSlug(
                                    attribute, request.getAttributeValueSlug().trim())
                            .orElseThrow(
                                    () ->
                                            new BadRequestException(
                                                    "Invalid value for attribute: "
                                                            + attribute.getName()));

            if (!attributeValue.isActive()) {
                throw new BadRequestException(
                        "Selected value is inactive for: " + attribute.getName());
            }

            productAttribute.setAttributeValue(attributeValue);
            return productAttribute;
        }

        if (attribute.getType() != AttributeType.TEXT) {
            throw new BadRequestException(
                    "Custom values are not permitted for: " + attribute.getName());
        }

        productAttribute.setCustomValue(request.getCustomValue().trim());

        return productAttribute;
    }

    public void validateRequiredVariantAttributes(
            Category category, Set<String> requestedAttributeSlugs) {
        List<CategoryAttribute> requiredAttributes =
                categoryAttributeRepository
                        .findByCategoryAndActiveTrueAndVariantAttributeTrueAndRequiredTrue(
                                category);

        for (CategoryAttribute attribute : requiredAttributes) {
            if (!requestedAttributeSlugs.contains(attribute.getSlug())) {
                throw new BadRequestException(
                        "Variant attribute '" + attribute.getName() + "' is required.");
            }
        }
    }

    public void populateAndValidateVariantAttributes(
            ProductVariant variant, List<VariantAttributeRequest> requests, Category category) {
        List<VariantAttributeRequest> attributeRequests = requests == null ? List.of() : requests;

        Set<String> requestedAttributeSlugs = new HashSet<>();

        for (VariantAttributeRequest request : attributeRequests) {
            String attributeSlug = request.getAttributeSlug().trim();

            if (!requestedAttributeSlugs.add(attributeSlug)) {
                throw new BadRequestException("Duplicate variant attribute: " + attributeSlug);
            }

            CategoryAttribute attribute =
                    categoryAttributeRepository
                            .findByCategoryAndSlug(category, attributeSlug)
                            .orElseThrow(
                                    () ->
                                            new BadRequestException(
                                                    "Attribute '"
                                                            + attributeSlug
                                                            + "' does not belong to category '"
                                                            + category.getName()
                                                            + "'."));

            if (!attribute.isActive()) {
                throw new BadRequestException(
                        "Attribute '" + attribute.getName() + "' is inactive.");
            }

            if (!attribute.isVariantAttribute()) {
                throw new BadRequestException(
                        "Attribute '"
                                + attribute.getName()
                                + "' cannot be used to create a product variant.");
            }

            ProductVariantAttribute variantAttribute =
                    createVariantAttribute(variant, attribute, request);

            variant.getAttributes().add(variantAttribute);
        }

        validateRequiredVariantAttributes(category, requestedAttributeSlugs);
    }

    public ProductVariantAttribute createVariantAttribute(
            ProductVariant variant, CategoryAttribute attribute, VariantAttributeRequest request) {
        boolean hasValueSlug =
                request.getAttributeValueSlug() != null
                        && !request.getAttributeValueSlug().isBlank();

        boolean hasCustomValue =
                request.getCustomValue() != null && !request.getCustomValue().isBlank();

        if (hasValueSlug == hasCustomValue) {
            throw new BadRequestException(
                    "Provide either attributeValueSlug or customValue for '"
                            + attribute.getName()
                            + "'.");
        }

        if (hasValueSlug) {
            AttributeValue attributeValue =
                    attributeValueRepository
                            .findByAttributeAndSlug(
                                    attribute, request.getAttributeValueSlug().trim())
                            .orElseThrow(
                                    () ->
                                            new BadRequestException(
                                                    "'"
                                                            + request.getAttributeValueSlug()
                                                            + "' is not a valid value for "
                                                            + attribute.getName()
                                                            + "."));

            if (!attributeValue.isActive()) {
                throw new BadRequestException(
                        "The selected value for " + attribute.getName() + " is inactive.");
            }

            return new ProductVariantAttribute(variant, attribute, attributeValue);
        }

        if (attribute.getType() != AttributeType.TEXT) {
            throw new BadRequestException(
                    "Custom values are not permitted for " + attribute.getName() + ".");
        }

        return new ProductVariantAttribute(variant, attribute, request.getCustomValue().trim());
    }
}
