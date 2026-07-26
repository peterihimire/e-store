package com.benkih.estore.product.controller;


import com.benkih.estore.common.dto.PaginatedResponse;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.inventory.service.IInventoryService;
import com.benkih.estore.product.dto.request.AddProductRequest;
import com.benkih.estore.product.dto.request.UpdateProductRequest;
import com.benkih.estore.product.dto.response.ProductPageResponseDto;
import com.benkih.estore.product.dto.response.ProductResponseDto;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.service.IProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
  private final IProductService productService;



  @GetMapping("/all")
  public ResponseEntity<ApiResponse> getAllProducts(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {

    // Convert to zero-based for Spring
    Pageable pageable = PageRequest.of(page - 1, limit);
    Page<ProductResponseDto> productPage = productService.getAllProducts(pageable);

    // Build paginated response
    PaginatedResponse<ProductResponseDto> response = PaginatedResponse.from(productPage, page);

    return ResponseEntity.ok(new ApiResponse("success", "Products retrieved", response));
  }

//  @GetMapping("/all")
//  public ResponseEntity<ApiResponse> getAllProducts() {
//    List<Product> productsData = productService.getAllProducts();
//    List<ProductResponseDto> products = productService.getConvertedProducts(productsData);
//    return ResponseEntity.ok(new ApiResponse("success","Product returned", products));
//  }
@GetMapping("/{slug}")
public ResponseEntity<ApiResponse> getProductBySlug(@PathVariable String slug) {
  try {
    Product product = productService.getProductBySlug(slug);
    ProductResponseDto productDto = productService.convertToDto(product);

    return ResponseEntity.ok(
        new ApiResponse("success", "product returned", productDto)
    );

  } catch (ResourceNotFoundException e) {
    return ResponseEntity.status(NOT_FOUND)
        .body(new ApiResponse("fail", e.getMessage(), null));
  }
}


//  @GetMapping("/{slug}")
//  public ResponseEntity<ApiResponse> getProductBySlug(@PathVariable String slug){
//    try {
//      Product product = productService.getProductBySlug(slug);
//      ProductResponseDto productDto = productService.convertToDto(product);
//      return ResponseEntity.ok(new ApiResponse("success","success", productDto));
//    } catch (ResourceNotFoundException e) {
//      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail",e.getMessage(), null));
//    }
//  }

  @GetMapping("/by/brand-and-name")
  public ResponseEntity<ApiResponse> getProductByBrandAndName(
      @RequestParam String brand,
      @RequestParam String name,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit,
      HttpServletRequest request){
    try {

      Pageable pageable = PageRequest.of(page - 1, limit);
      Page<ProductResponseDto> productPage =
          productService.getProductsByBrandAndName(brand, name, pageable);

      PaginatedResponse<ProductResponseDto> response = PaginatedResponse.from(productPage, page);

      return ResponseEntity.ok(new ApiResponse("success","Products returned",
          response));
    } catch (Exception e) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }

  @GetMapping("/by/category-and-brand")
  public ResponseEntity<ApiResponse> getProductByCategoryAndBrand(@RequestParam String category, @RequestParam String brand){
    try {
      List<Product> productsData = productService.getProductsByCategoryAndBrand(category, brand);

      if(productsData.isEmpty()){
        return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail","No product found", null));
      }
      List<ProductResponseDto> products = productService.getConvertedProducts(productsData);
      return ResponseEntity.ok(new ApiResponse("success","Products returned", products));
    } catch (Exception e) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }

  @GetMapping("/by-name")
  public ResponseEntity<ApiResponse> getProductByCategoryAndBrand(@RequestParam String name){
    try {
      List<Product> productsData = productService.getProductsByName(name);
      if(productsData.isEmpty()){
        return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail","No product found", null));
      }
      List<ProductResponseDto> products = productService.getConvertedProducts(productsData);
      return ResponseEntity.ok(new ApiResponse("success","Products returned", products));
    } catch (Exception e) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }

  @GetMapping("/by-brand")
  public ResponseEntity<ApiResponse> getProductByBrand(@RequestParam String brand){
    try {
      List<Product> productsData = productService.getProductsByBrand(brand);

      if(productsData.isEmpty()){
        return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail","No product found", null));
      }
      List<ProductResponseDto> products = productService.getConvertedProducts(productsData);
      return ResponseEntity.ok(new ApiResponse("success","Products returned", products));
    } catch (Exception e) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }

  @GetMapping("/by-category")
  public ResponseEntity<ApiResponse> getProductByCategory(@RequestParam String category){
    try {
      List<Product> productsData = productService.getProductsByCategory(category);
      if(productsData.isEmpty()){
        return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail","No product found", null));
      }
      List<ProductResponseDto> products = productService.getConvertedProducts(productsData);
      return ResponseEntity.ok(new ApiResponse("success","Products returned", products));
    } catch (Exception e) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/addProduct")
  public ResponseEntity<ApiResponse> addProduct(@RequestBody AddProductRequest product){ // uses general exception to throw error
      Product productData = productService.addProduct(product);
      ProductResponseDto productDto = productService.convertToDto(productData);
      return ResponseEntity.ok(new ApiResponse("success","Product added", productDto));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{productSlug}/update")
  public ResponseEntity<ApiResponse> updateProduct(@RequestBody UpdateProductRequest product, @PathVariable String productSlug){
    try {
      Product productData = productService.updateProduct(product, productSlug);
      ProductResponseDto productDto = productService.convertToDto(productData);
      return ResponseEntity.ok(new ApiResponse("success","Product updated", productDto));
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/product/{productId}/delete")
  public ResponseEntity<ApiResponse> deleteProduct(@PathVariable String productId){
    try {
     productService.deleteProductById(productId);
      return ResponseEntity.ok(new ApiResponse("success","Product deleted", null));
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }

  @GetMapping("/product/count/brand-and-name")
  public ResponseEntity<ApiResponse> countProductsByBrandAndName(@RequestParam String brand, @RequestParam String name){
    try {
      Long productCount = productService.countProductByBrandAndName(brand, name);
      return ResponseEntity.ok(new ApiResponse("success","Products returned", productCount));
    } catch (Exception e) {
      return ResponseEntity.ok(new ApiResponse("fail",e.getMessage(), null));
    }
  }
}
