package com.benkih.estore.product.controller;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.benkih.estore.common.dto.PaginatedResponse;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.product.dto.request.AddProductRequest;
import com.benkih.estore.product.dto.request.UpdateProductRequest;
import com.benkih.estore.product.dto.response.ProductResponseDto;
import com.benkih.estore.product.service.ProductService;
import com.benkih.estore.security.tenant.TenantContext;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirements(value = {})
@Tag(name = "Products", description = "Product catalogue and search")
@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final TenantContext tenantContext;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductResponseDto>>> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        // Convert to zero-based for Spring
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ProductResponseDto> productPage = productService.getAllProducts(pageable);

        // Build paginated response
        PaginatedResponse<ProductResponseDto> response = PaginatedResponse.from(productPage, page);

        return ResponseEntity.ok(new ApiResponse<>("success", "Products retrieved", response));
    }

    //  @GetMapping("/all")
    //  public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getAllProducts() {
    //    List<ProductResponseDto> products = productService.getAllProducts();
    //    List<ProductResponseDto> products = productService.getConvertedProducts(productsData);
    //    return ResponseEntity.ok(new ApiResponse<>("success","Product returned", products));
    //  }
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductBySlug(
            @PathVariable String slug) {
        try {
            ProductResponseDto productDto = productService.getProductBySlug(slug);
            //    ProductResponseDto productDto = productService.convertToDto(product);

            return ResponseEntity.ok(new ApiResponse<>("success", "product returned", productDto));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    //  @GetMapping("/{slug}")
    //  public ResponseEntity<ApiResponse<ProductResponseDto>> getProductBySlug(@PathVariable String
    // slug){
    //    try {
    //      Product product = productService.getProductBySlug(slug);
    //      ProductResponseDto productDto = productService.convertToDto(product);
    //      return ResponseEntity.ok(new ApiResponse<>("success","success", productDto));
    //    } catch (ResourceNotFoundException e) {
    //      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse<>("fail",e.getMessage(),
    // null));
    //    }
    //  }

    @GetMapping("/by/brand-and-name")
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductResponseDto>>>
            getProductByBrandAndName(
                    @RequestParam String brand,
                    @RequestParam String name,
                    @RequestParam(defaultValue = "1") int page,
                    @RequestParam(defaultValue = "10") int limit,
                    HttpServletRequest request) {
        try {

            Pageable pageable = PageRequest.of(page - 1, limit);
            Page<ProductResponseDto> productPage =
                    productService.getProductsByBrandAndName(brand, name, pageable);

            PaginatedResponse<ProductResponseDto> response =
                    PaginatedResponse.from(productPage, page);

            return ResponseEntity.ok(new ApiResponse<>("success", "Products returned", response));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @GetMapping("/by/category-and-brand")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getProductByCategoryAndBrand(
            @RequestParam String category, @RequestParam String brand) {
        try {
            List<ProductResponseDto> products =
                    productService.getProductsByCategoryAndBrand(category, brand);

            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND)
                        .body(new ApiResponse<>("fail", "No product found", null));
            }
            return ResponseEntity.ok(new ApiResponse<>("success", "Products returned", products));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @GetMapping("/by-name")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getProductByCategoryAndBrand(
            @RequestParam String name) {
        try {
            List<ProductResponseDto> products = productService.getProductsByName(name);
            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND)
                        .body(new ApiResponse<>("fail", "No product found", null));
            }
            return ResponseEntity.ok(new ApiResponse<>("success", "Products returned", products));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @GetMapping("/by-brand")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getProductByBrand(
            @RequestParam String brand) {
        try {
            List<ProductResponseDto> products = productService.getProductsByBrand(brand);

            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND)
                        .body(new ApiResponse<>("fail", "No product found", null));
            }
            return ResponseEntity.ok(new ApiResponse<>("success", "Products returned", products));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @GetMapping("/by-category")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getProductByCategory(
            @RequestParam String category) {
        try {
            List<ProductResponseDto> products = productService.getProductsByCategory(category);
            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND)
                        .body(new ApiResponse<>("fail", "No product found", null));
            }
            return ResponseEntity.ok(new ApiResponse<>("success", "Products returned", products));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @SecurityRequirement(name = "bearer-jwt")
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    @PostMapping("/addProduct")
    public ResponseEntity<ApiResponse<ProductResponseDto>> addProduct(
            @RequestBody AddProductRequest product) { // uses general exception to throw error
        Long businessId = tenantContext.getBusinessId();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "Product added",
                        productService.addProduct(product, businessId)));
    }

    @SecurityRequirement(name = "bearer-jwt")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    @PutMapping("/{productSlug}/update")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
            @RequestBody UpdateProductRequest product, @PathVariable String productSlug) {
        Long businessId = tenantContext.getBusinessId();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "Product updated",
                        productService.updateProduct(product, productSlug, businessId)));
    }

    @SecurityRequirement(name = "bearer-jwt")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    @DeleteMapping("/product/{productSlug}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String productSlug) {
        Long businessId = tenantContext.getBusinessId();

        try {
            productService.deleteProductById(productSlug, businessId);
            return ResponseEntity.ok(new ApiResponse<>("success", "Product deleted", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @GetMapping("/product/count/brand-and-name")
    public ResponseEntity<ApiResponse<Long>> countProductsByBrandAndName(
            @RequestParam String brand, @RequestParam String name) {
        try {
            Long productCount = productService.countProductByBrandAndName(brand, name);
            return ResponseEntity.ok(
                    new ApiResponse<>("success", "Products returned", productCount));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }
}
