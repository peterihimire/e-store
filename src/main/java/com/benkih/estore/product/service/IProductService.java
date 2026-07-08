package com.benkih.estore.product.service;
import com.benkih.estore.product.dto.request.AddProductRequest;
import com.benkih.estore.product.dto.request.UpdateProductRequest;
import com.benkih.estore.product.dto.response.ProductResponseDto;
import com.benkih.estore.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Here is abstract class for product service
public interface IProductService {
  Product addProduct(AddProductRequest product);
  List<Product> getAllProducts();

  //  @Override
  //  public List<Product> getAllProducts() {
  //    return productRepository.findAll();
  //  }

  @Transactional(readOnly = true)
  Page<ProductResponseDto> getAllProducts(int page, int limit);

  @Transactional(readOnly = true)
  List<ProductResponseDto> getConvertedProducts(List<Product> products);

  ProductResponseDto convertToDto(Product product);

  List<Product> getProductsByCategory(String category);
  List<Product> getProductsByBrand(String brand);
  List<Product> getProductsByCategoryAndBrand(String category, String brand);
  List<Product> getProductsByName(String name);
  List<Product> getProductsByBrandAndName(String brand, String name);
  //  Product getProductById(Long id);
  @EntityGraph(attributePaths = "images")
  Product getProductBySlug(String slug);
  Product updateProduct(UpdateProductRequest product, String slug);
  void deleteProductById(String slug);
  Long countProductByBrandAndName(String brand, String name);

  Page<ProductResponseDto> getAllProducts(Pageable pageable);
  Page<ProductResponseDto> getProductsByBrandAndName(String brand, String name,
                                          Pageable pageable);
}
