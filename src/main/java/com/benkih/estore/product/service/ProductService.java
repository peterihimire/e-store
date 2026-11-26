package com.benkih.estore.product.service;

import com.benkih.estore.product.dto.request.AddProductRequest;
import com.benkih.estore.product.dto.request.UpdateProductRequest;
import com.benkih.estore.product.dto.response.ProductResponseDto;
import com.benkih.estore.product.entity.Product;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponseDto addProduct(AddProductRequest request, Long businessId);

    Page<ProductResponseDto> getAllProducts(int page, int limit);

    List<ProductResponseDto> getConvertedProducts(List<Product> products);

    ProductResponseDto convertToDto(Product product);

    List<ProductResponseDto> getProductsByCategory(String category);

    List<ProductResponseDto> getProductsByBrand(String brandSlug);

    List<ProductResponseDto> getProductsByCategoryAndBrand(String category, String brand);

    List<ProductResponseDto> getProductsByName(String name);

    List<ProductResponseDto> getProductsByBrandAndName(String brand, String name);

    ProductResponseDto getProductBySlug(String slug);

    ProductResponseDto updateProduct(UpdateProductRequest request, String slug, Long businessId);

    void deleteProductById(String slug, Long businessId);

    Long countProductByBrandAndName(String brand, String name);

    Page<ProductResponseDto> getAllProducts(Pageable pageable);

    Page<ProductResponseDto> getProductsByBrandAndName(
            String brandSlug, String name, Pageable pageable);
}
