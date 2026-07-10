package com.benkih.estore.product.service;

import com.benkih.estore.product.entity.Category;
import com.benkih.estore.product.repository.CategoryRepository;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.product.dto.response.ImageDto;
import com.benkih.estore.product.dto.request.AddProductRequest;
import com.benkih.estore.product.dto.request.UpdateProductRequest;
import com.benkih.estore.product.dto.response.ProductResponseDto;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements IProductService{
  private final ProductRepository productRepository; // final keyword inject the ProductRepository properly
  private final CategoryRepository categoryRepository;

  @Override
  public Product addProduct(AddProductRequest request) {

    if(productExists(request.getName(), request.getBrand())){
      throw new AlreadyExistsException(request.getBrand() + " " + request.getName() + " already exists, you may update this product instead." );
    }

    Category category = Optional.ofNullable(categoryRepository.findByName(request.getCategory()))
        .orElseGet(() -> {
            Category newCategory = new Category(request.getCategory());// Remember this place had issues
            return categoryRepository.save(newCategory);
    });
//    request.setCategory(category);
    return productRepository.save(createProduct(request, category));
  }



  private boolean productExists(String name, String brand ){
    return productRepository.existsByNameAndBrand(name, brand);
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


//  @Transactional(readOnly = true)
  @Override
  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

@Transactional(readOnly = true)
@Override
public Page<ProductResponseDto> getAllProducts(int page, int limit) {

  Pageable pageable = PageRequest.of(page, limit);

  return productRepository
      .findAll(pageable)
      .map(this::convertToDto);
}

//  @Transactional(readOnly = true)
  @Override
  public List<ProductResponseDto> getConvertedProducts(List<Product> products) {
    return products.stream().map(this::convertToDto).toList();
  }

  @Transactional(readOnly = true)
  @Override
  public ProductResponseDto convertToDto(Product product) {

    List<ImageDto> imageDtos = Optional.ofNullable(product.getImages())
        .orElse(List.of())
        .stream()
        .map(image -> new ImageDto(
            image.getSlug(),
            image.getFileName(),
            image.getDownloadUrl()
        ))
        .toList();

    return new ProductResponseDto(
        product.getSlug(),
        product.getName(),
        product.getBrand(),
        product.getDescription(),
        product.getPrice(),
        product.getInventory(),
        product.getCategory() != null ? product.getCategory().getName() : null,
        // product.getCategory().getName(),
        imageDtos
    );
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
    return List.of();
  }

//  @Override
//  public List<Product> getProductsByBrandAndName(String brand, String name) {
//    return productRepository.findByBrandAndName(brand, name);
//  }

  //  @Override
  //  public Product getProductById(Long id) {
  //    return productRepository.findById(id)
  //        .orElseThrow(()-> new NotFoundException("Product not found!"));
  //  }

  //  @Transactional(readOnly = true)
  @Override
  public Product getProductBySlug(String slug) {
    Product product = productRepository.findBySlug(slug)
        .orElseThrow(()-> new ResourceNotFoundException("Product not found!"));

    return product;
  }

  @Override
  public Product updateProduct(UpdateProductRequest request, String slug) {
    Product updatedProduct = productRepository.findBySlug(slug)
        .map(existingProduct -> updateExistingProduct(existingProduct, request))
        .map(productRepository::save)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));

    return updatedProduct;

  }

  private Product updateExistingProduct(Product existingProduct, UpdateProductRequest request) {
    if (request.getName() != null) {
      existingProduct.setName(request.getName());
    }
    if (request.getBrand() != null) {
      existingProduct.setBrand(request.getBrand());
    }
    if (request.getDescription() != null) {
      existingProduct.setDescription(request.getDescription());
    }
    if (request.getPrice() != null) {
      existingProduct.setPrice(request.getPrice());
    }
    if (request.getInventory() != null) {
      existingProduct.setInventory(request.getInventory());
    }

    if (request.getCategoryName() != null) {
      Category category = categoryRepository.findByName(request.getCategoryName());
      if (category == null) {
        throw new ResourceNotFoundException("Category not found");
      }
      existingProduct.setCategory(category);
    }

    return existingProduct;
  }


  @Override
  public void deleteProductById(String slug) {
    productRepository.findBySlug(slug).ifPresentOrElse(productRepository::delete,
        () -> {throw new ResourceNotFoundException("Product not found!");});
  }

  @Override
  public Long countProductByBrandAndName(String brand, String name) {
    return productRepository.countByBrandAndName(brand, name);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
    log.info("Fetching products with page: {}, size: {}",
        pageable.getPageNumber(), pageable.getPageSize());


      Page<Product> productPage = productRepository.findAll(pageable);

      if (productPage.isEmpty()) {
        log.info("No products found for page: {}", pageable.getPageNumber());
        return Page.empty(pageable);
      }

      return productPage.map(this::convertToDto);

  }

  @Override
  public Page<ProductResponseDto> getProductsByBrandAndName(String brand, String name,
                                                  Pageable pageable) {
    log.info("Fetching products with page: {}, size: {}",
        pageable.getPageNumber(), pageable.getPageSize());


      Page<Product> productPage =
          productRepository.findByBrandAndName(brand, name, pageable);

      if (productPage.isEmpty()) {
        log.info("No products found for brand: {} and name: {}", brand, name);
        return Page.empty(pageable);
      }

      return productPage.map(this::convertToDto);

  }
}
