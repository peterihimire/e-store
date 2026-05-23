package com.benkih.estore.product.service;
import com.benkih.estore.product.dto.request.AddProductRequest;
import com.benkih.estore.product.dto.request.UpdateProductRequest;
import com.benkih.estore.product.dto.response.ProductsResponseDto;
import com.benkih.estore.product.entity.Product;

import java.util.List;

// Here is abstract class for product service
public interface IProductService {
  Product addProduct(AddProductRequest product);
  List<ProductsResponseDto> getAllProducts();
  List<Product> getProductsByCategory(String category);
  List<Product> getProductsByBrand(String brand);
  List<Product> getProductsByCategoryAndBrand(String category, String brand);
  List<Product> getProductsByName(String name);
  List<Product> getProductsByBrandAndName(String brand, String name);
  //  Product getProductById(Long id);
  ProductsResponseDto getProductBySlug(String slug);
  Product updateProduct(UpdateProductRequest product, String slug);
  void deleteProductById(String slug);
  Long countProductByBrandAndName(String brand, String name);
}
