package com.benkih.estore.product.service;

import com.benkih.estore.category.entity.Category;
import com.benkih.estore.category.repository.CategoryRepository;
import com.benkih.estore.common.exceptions.ProductNotFoundException;
import com.benkih.estore.product.dto.request.AddProductRequest;
import com.benkih.estore.product.dto.request.UpdateProductRequest;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService{
  private final ProductRepository productRepository; // final keyword inject the ProductRepository properly
  private final CategoryRepository categoryRepository;

  @Override
  public Product addProduct(AddProductRequest request) {

    Category category = Optional.ofNullable(categoryRepository.findByName(request.getCategory().getName()))
        .orElseGet(() -> {
            Category newCategory = new Category(request.getCategory().getName());// Remember this place had issues
            return categoryRepository.save(newCategory);
    });
    request.setCategory(category);
    return productRepository.save(createProduct(request, category));
  }

  private Product createProduct(AddProductRequest request, Category category){
    return new Product(
        request.getName(),
        request.getBrand(),
        request.getDescription(),
        request.getPrice(),
        request.getInventory(),
        category
    );
  }

  @Override
  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  @Override
  public List<Product> getProductsByCategory(String category) {
    return productRepository.findByCategoryName(category);
  }

  @Override
  public List<Product> getProductsByBrand(String brand) {
    return productRepository.findByBrand(brand);
  }

  @Override
  public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
    return productRepository.findByCategoryNameAndBrand(category, brand);
  }

  @Override
  public List<Product> getProductsByName(String name) {
    return productRepository.findByName(name);
  }

  @Override
  public List<Product> getProductsByBrandAndName(String brand, String name) {
    return productRepository.findByBrandAndName(brand, name);
  }

  @Override
  public Product getProductById(String slug) {
    return productRepository.findById(slug)
        .orElseThrow(()-> new ProductNotFoundException("Product not found!"));
  }

  @Override
  public Product updateProduct(UpdateProductRequest request, String slug) {
  return  productRepository.findById(slug)
        .map(existingProduct -> updateExistingProduct(existingProduct, request))
        .map(productRepository :: save)
        .orElseThrow(() -> new ProductNotFoundException("Product not found!"));
  }

  private Product updateExistingProduct(Product existingProduct, UpdateProductRequest request){
    existingProduct.setName(request.getName());
    existingProduct.setBrand(request.getBrand());
    existingProduct.setDescription(request.getDescription());
    existingProduct.setPrice(request.getPrice());
    existingProduct.setInventory(request.getInventory());

    Category category = categoryRepository.findByName(request.getCategory().getName());
    existingProduct.setCategory(category);
    return existingProduct;
  }

  @Override
  public void deleteproductById(String slug) {
    productRepository.findById(slug).ifPresentOrElse(productRepository::delete,
        () -> {throw new ProductNotFoundException("Product not found!");});
  }

  @Override
  public Long countProductByBrandAndName(String brand, String name) {
    return productRepository.countByBrandAndName(brand, name);
  }
}
