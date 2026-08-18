package com.benkih.estore.product.service;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.business.repository.BusinessRepository;
import com.benkih.estore.common.enums.ProductStatus;
import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.inventory.repository.InventoryRepository;
import com.benkih.estore.inventory.service.IInventoryService;
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
import com.benkih.estore.product.repository.SkuSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements IProductService{
  private final ProductRepository productRepository; // final keyword inject the ProductRepository properly
  private final CategoryRepository categoryRepository;
  private final InventoryRepository inventoryRepository;
  private final IInventoryService inventoryService;
  private final SkuSequenceRepository skuSequenceRepository;
  private final BusinessRepository businessRepository;


  @Transactional
  @Override
  public Product addProduct(AddProductRequest request, Long businessId) {

    if(productExists(request.getName(), request.getBrand(), businessId)){
      throw new AlreadyExistsException(
          request.getBrand() + " " + request.getName() + " already exists, you may update this product instead." );
    }

    Business business = businessRepository.findById(businessId)
        .orElseThrow(() ->
            new ResourceNotFoundException("Business not found"));

    Category category =
        Optional.ofNullable(categoryRepository.findByNameAndBusinessId(request.getCategory(), businessId))
        .orElseGet(() -> {
            Category newCategory = new Category(request.getCategory());
            newCategory.setBusiness(business);
            return categoryRepository.save(newCategory);
    });

    Product product = createProduct(request, category, business);
    product.setSku(generateSku(product));
    product = productRepository.save(product);
    inventoryService.createInventory(product);

    return product;
  }

  private boolean productExists(String name, String brand, Long businessId ){
    return productRepository.existsByNameAndBrandAndBusinessId(
        name,
        brand,
        businessId);
  }

  private Product createProduct(
      AddProductRequest request,
      Category category,
      Business business){
    return new Product(
        request.getName(),
        request.getBrand(),
        request.getDescription(),
        request.getPrice(),
        category,
        business
    );
  }


  @Override
  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }


  @Transactional(readOnly = true)
  @Override
  public Page<ProductResponseDto> getAllProducts(int page, int limit) {
    Pageable pageable = PageRequest.of(page, limit);
    Page<Product> productPage = productRepository.findAll(pageable);
    List<ProductResponseDto> dtos = convertProducts(productPage.getContent());

    return new PageImpl<>(
        dtos,
        pageable,
        productPage.getTotalElements()
    );
  }


  @Override
  public List<ProductResponseDto> getConvertedProducts(List<Product> products) {
    return convertProducts(products);
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
        product.getSku(),
        product.getStatus(),
        product.getName(),
        product.getBrand(),
        product.getDescription(),
        product.getPrice(),
        product.getInventory().getAvailableStock(),
        product.getInventory().getAvailableStock() > 0,
        product.getCategory() != null ? product.getCategory().getName() : null,
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


  @Override
  public Product getProductBySlug(String slug) {
    Product product = productRepository.findBySlug(slug)
        .orElseThrow(()-> new ResourceNotFoundException("Product not found!"));

    return product;
  }


  @Override
  public Product updateProduct(UpdateProductRequest request, String slug,
                               Long businessId) {
    Product updatedProduct = productRepository.findBySlug(slug)
        .map(existingProduct -> updateExistingProduct(existingProduct,
            request, businessId))
        .map(productRepository::save)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));

    return updatedProduct;
  }


  private String generateSku(Product product) {
    long sequence = skuSequenceRepository.nextSkuNumber();
    String brandCode = abbreviate(product.getBrand());
    String productCode = abbreviate(product.getName());

    return String.format(
        "%s-%s-%04d",
        brandCode,
        productCode,
        sequence
    );
  }


  private String abbreviate(String value) {
    String cleaned = value
        .replaceAll("[^A-Za-z]", "")
        .toUpperCase();

    return cleaned.substring(0, Math.min(3, cleaned.length()));
  }


  private Product updateExistingProduct(Product existingProduct,
                                        UpdateProductRequest request,
                                        Long businessId) {
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
    if (request.getCategoryName() != null) {
      Category category =
          categoryRepository.findByNameAndBusinessId(request.getCategoryName(), businessId);
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
    log.info("Fetching products with page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

      Page<Product> productPage = productRepository.findAll(pageable);

      if (productPage.isEmpty()) {
        log.info("No products found for page: {}", pageable.getPageNumber());
        return Page.empty(pageable);
      }

    List<ProductResponseDto> dtos = convertProducts(productPage.getContent());

    return new PageImpl<>(
        dtos,
        pageable,
        productPage.getTotalElements());

  }

  @Override
  public Page<ProductResponseDto> getProductsByBrandAndName(
      String brand,
      String name,
      Pageable pageable) {
//    log.info("Fetching products with page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
      Page<Product> productPage = productRepository.findByBrandAndName(brand, name, pageable);

      if (productPage.isEmpty()) {
//        log.info("No products found for brand: {} and name: {}", brand, name);
        return Page.empty(pageable);
      }

    List<ProductResponseDto> dtos =
        convertProducts(productPage.getContent());

    return new PageImpl<>(
        dtos,
        pageable,
        productPage.getTotalElements());
  }

  private List<ProductResponseDto> convertProducts(List<Product> products) {
    List<String> slugs = products.stream()
        .map(Product::getSlug)
        .toList();

    return products.stream()
        .map(product -> convertToDto(product))
        .toList();
  }
}

