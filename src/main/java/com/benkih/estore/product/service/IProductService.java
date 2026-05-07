package com.benkih.estore.product.service;

import com.benkih.estore.product.dto.request.AddProductRequest;
import com.benkih.estore.product.dto.request.UpdateProductRequest;
import com.benkih.estore.product.entity.Product;

import java.util.List;

public interface IProductService {
  Product addProduct(AddProductRequest product);
  List<Product> getAllProducts();
  List<Product> getProductsByCategory(String category);
  List<Product> getProductsByBrand(String brand);
  List<Product> getProductsByCategoryAndBrand(String category, String brand);
  List<Product> getProductsByName(String name);
  List<Product> getProductsByBrandAndName(String brand, String name);
  Product getProductById(String slug);
  Product updateProduct(UpdateProductRequest product, String slug);
  void deleteproductById(String slug);
  Long countProductByBrandAndName(String brand, String name);
}
