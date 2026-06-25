package com.benkih.estore.product.controller;


import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.product.dto.request.AddProductRequest;
import com.benkih.estore.product.dto.request.UpdateProductRequest;
import com.benkih.estore.product.dto.response.ProductResponseDto;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
  private final IProductService productService;

  @GetMapping("/all")
  public ResponseEntity<ApiResponse> getAllProducts() {
    List<Product> productsData = productService.getAllProducts();
    List<ProductResponseDto> products = productService.getConvertedProducts(productsData);
    return ResponseEntity.ok(new ApiResponse("success","Product returned", products));
  }

  //  @GetMapping("/{id}")
  //  public ResponseEntity<ApiResponse> getProductById(@PathVariable Long id){
  //    try {
  //      Product product = productService.getProductById(id);
  //      return ResponseEntity.ok(new ApiResponse("success", product));
  //    } catch (NotFoundException e) {
  //      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
  //    }
  //  }

  @GetMapping("/{slug}")
  public ResponseEntity<ApiResponse> getProductBySlug(@PathVariable String slug){
    try {
      Product product = productService.getProductBySlug(slug);
      ProductResponseDto productDto = productService.convertToDto(product);
      return ResponseEntity.ok(new ApiResponse("success","success", productDto));
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }

  @GetMapping("/by/brand-and-name")
  public ResponseEntity<ApiResponse> getProductByBrandAndName(@RequestParam String brand, @RequestParam String name){
    try {
      List<Product> productsData = productService.getProductsByBrandAndName(brand, name);

      if(productsData.isEmpty()){
        return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fauk","No product found", null));
      }

      List<ProductResponseDto> products = productService.getConvertedProducts(productsData);
      return ResponseEntity.ok(new ApiResponse("success","Products returned", products));
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

  @PostMapping("/addProduct")
  public ResponseEntity<ApiResponse> addProduct(@RequestBody AddProductRequest product){
    try {
      Product productData = productService.addProduct(product);
      ProductResponseDto productDto = productService.convertToDto(productData);
      return ResponseEntity.ok(new ApiResponse("success","Product added", productDto));
    } catch (Exception e) {
      return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }

  @PutMapping("/{productId}/update")
  public ResponseEntity<ApiResponse> updateProduct(@RequestBody UpdateProductRequest product, @PathVariable String productId){
    try {
      Product productData = productService.updateProduct(product, productId);
      ProductResponseDto productDto = productService.convertToDto(productData);
      return ResponseEntity.ok(new ApiResponse("success","Product updated", productDto));
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail",e.getMessage(), null));
    }
  }

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
