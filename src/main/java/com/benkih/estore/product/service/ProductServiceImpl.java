package com.benkih.estore.product.service;

import com.benkih.estore.product.dto.request.AddProductRequest;
import com.benkih.estore.product.dto.request.UpdateProductRequest;
import com.benkih.estore.product.dto.response.ProductResponseDto;
import com.benkih.estore.product.entity.Product;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductCatalogService productCatalogService;
    private final ProductMutationService productMutationService;

    @Transactional
    @Override
    public ProductResponseDto addProduct(AddProductRequest request, Long businessId) {
        return productMutationService.addProduct(request, businessId);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductResponseDto> getAllProducts(int page, int limit) {
        return productCatalogService.getAllProducts(page, limit);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductResponseDto> getConvertedProducts(List<Product> products) {
        return productCatalogService.getConvertedProducts(products);
    }

    @Override
    public ProductResponseDto convertToDto(Product product) {
        return productCatalogService.convertToDto(product);
    }

    @Override
    public List<ProductResponseDto> getProductsByCategory(String category) {
        return productCatalogService.getProductsByCategory(category);
    }

    @Override
    public List<ProductResponseDto> getProductsByBrand(String brandSlug) {
        return productCatalogService.getProductsByBrand(brandSlug);
    }

    @Override
    public List<ProductResponseDto> getProductsByCategoryAndBrand(String category, String brand) {
        return productCatalogService.getProductsByCategoryAndBrand(category, brand);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductResponseDto> getProductsByName(String name) {
        return productCatalogService.getProductsByName(name);
    }

    @Override
    public List<ProductResponseDto> getProductsByBrandAndName(String brand, String name) {
        return productCatalogService.getProductsByBrandAndName(brand, name);
    }

    @Transactional(readOnly = true)
    @Override
    public ProductResponseDto getProductBySlug(String slug) {
        return productCatalogService.getProductBySlug(slug);
    }

    @Override
    public ProductResponseDto updateProduct(
            UpdateProductRequest request, String slug, Long businessId) {
        return productMutationService.updateProduct(request, slug, businessId);
    }

    @Override
    public void deleteProductById(String slug, Long businessId) {
        productMutationService.deleteProductById(slug, businessId);
    }

    @Override
    public Long countProductByBrandAndName(String brand, String name) {
        return productCatalogService.countProductByBrandAndName(brand, name);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        return productCatalogService.getAllProducts(pageable);
    }

    @Override
    public Page<ProductResponseDto> getProductsByBrandAndName(
            String brandSlug, String name, Pageable pageable) {
        return productCatalogService.getProductsByBrandAndName(brandSlug, name, pageable);
    }
}
