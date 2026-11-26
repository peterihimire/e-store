package com.benkih.estore.product.service;

import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.product.dto.response.BrandResponseDto;
import com.benkih.estore.product.dto.response.ImageDto;
import com.benkih.estore.product.dto.response.ProductAttributeResponseDto;
import com.benkih.estore.product.dto.response.ProductResponseDto;
import com.benkih.estore.product.dto.response.ProductVariantResponseDto;
import com.benkih.estore.product.dto.response.VariantAttributeResponseDto;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.entity.ProductVariant;
import com.benkih.estore.product.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCatalogService {
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Product> productPage = productRepository.findAll(pageable);
        List<ProductResponseDto> dtos = convertProducts(productPage.getContent());

        return new PageImpl<>(dtos, pageable, productPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getConvertedProducts(List<Product> products) {
        return convertProducts(products);
    }

    public ProductResponseDto convertToDto(Product product) {
        List<ImageDto> imageDtos =
                Optional.ofNullable(product.getImages()).orElse(List.of()).stream()
                        .map(
                                image ->
                                        new ImageDto(
                                                image.getSlug(),
                                                image.getFileName(),
                                                image.getDownloadUrl()))
                        .toList();

        List<ProductVariantResponseDto> variantDtos =
                Optional.ofNullable(product.getVariants()).orElse(List.of()).stream()
                        .map(this::convertVariantToDto)
                        .toList();

        List<ProductAttributeResponseDto> attributeDtos =
                Optional.ofNullable(product.getAttributes()).orElse(List.of()).stream()
                        .map(
                                attribute ->
                                        new ProductAttributeResponseDto(
                                                attribute.getAttribute().getSlug(),
                                                attribute.getAttribute().getName(),
                                                attribute.getAttributeValue() != null
                                                        ? attribute.getAttributeValue().getSlug()
                                                        : null,
                                                attribute.getAttributeValue() != null
                                                        ? attribute.getAttributeValue().getValue()
                                                        : attribute.getCustomValue()))
                        .toList();

        BrandResponseDto brandDto =
                Optional.ofNullable(product.getBrand())
                        .map(
                                brand ->
                                        new BrandResponseDto(
                                                brand.getSlug(),
                                                brand.getName(),
                                                brand.getLogoUrl(),
                                                brand.getWebsiteUrl()))
                        .orElse(null);

        return new ProductResponseDto(
                product.getSlug(),
                product.getStatus(),
                product.getName(),
                product.getDescription(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                imageDtos,
                variantDtos,
                attributeDtos,
                brandDto);
    }

    private ProductVariantResponseDto convertVariantToDto(ProductVariant variant) {

        Inventory inventory = variant.getInventory();

        List<VariantAttributeResponseDto> attributes =
                Optional.ofNullable(variant.getAttributes()).orElse(List.of()).stream()
                        .map(
                                attribute -> {
                                    String value =
                                            attribute.getAttributeValue() != null
                                                    ? attribute.getAttributeValue().getValue()
                                                    : attribute.getCustomValue();

                                    return new VariantAttributeResponseDto(
                                            attribute.getAttribute().getName(), value);
                                })
                        .toList();

        return new ProductVariantResponseDto(
                variant.getSlug(),
                variant.getSku(),
                variant.getPrice(),
                variant.getCurrency(),
                variant.isActive(),
                inventory != null ? inventory.getAvailableStock() : 0,
                inventory != null && inventory.getAvailableStock() > 0,
                attributes);
    }

    public List<ProductResponseDto> getProductsByCategory(String category) {
        return productRepository.findByCategoryName(category).stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<ProductResponseDto> getProductsByBrand(String brandSlug) {
        return productRepository.findByBrandSlug(brandSlug).stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<ProductResponseDto> getProductsByCategoryAndBrand(String category, String brand) {
        return productRepository.findByCategoryNameAndBrandSlug(category, brand).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProductsByName(String name) {
        return productRepository.findByName(name).stream().map(this::convertToDto).toList();
    }

    public List<ProductResponseDto> getProductsByBrandAndName(String brand, String name) {
        return List.of();
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductBySlug(String slug) {
        Product product =
                productRepository
                        .findBySlug(slug)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));

        return convertToDto(product);
    }

    public Long countProductByBrandAndName(String brand, String name) {
        return productRepository.countByBrandSlugAndName(brand, name);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        log.info(
                "Fetching products with page: {}, size: {}",
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<Product> productPage = productRepository.findAll(pageable);

        if (productPage.isEmpty()) {
            log.info("No products found for page: {}", pageable.getPageNumber());
            return Page.empty(pageable);
        }

        List<ProductResponseDto> dtos = convertProducts(productPage.getContent());

        return new PageImpl<>(dtos, pageable, productPage.getTotalElements());
    }

    public Page<ProductResponseDto> getProductsByBrandAndName(
            String brandSlug, String name, Pageable pageable) {
        Page<Product> productPage =
                productRepository.findByBrandSlugAndName(brandSlug, name, pageable);

        if (productPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<ProductResponseDto> dtos = convertProducts(productPage.getContent());

        return new PageImpl<>(dtos, pageable, productPage.getTotalElements());
    }

    private List<ProductResponseDto> convertProducts(List<Product> products) {
        List<String> slugs = products.stream().map(Product::getSlug).toList();

        return products.stream().map(product -> convertToDto(product)).toList();
    }
}
