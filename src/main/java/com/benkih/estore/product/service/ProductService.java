package com.benkih.estore.product.service;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.business.repository.BusinessRepository;
import com.benkih.estore.common.enums.AttributeType;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.inventory.repository.InventoryRepository;
import com.benkih.estore.inventory.service.IInventoryService;
import com.benkih.estore.product.dto.request.CreateProductVariantRequest;
import com.benkih.estore.product.dto.request.VariantAttributeRequest;
import com.benkih.estore.product.dto.response.ProductVariantResponseDto;
import com.benkih.estore.product.dto.response.VariantAttributeResponseDto;
import com.benkih.estore.product.entity.*;
import com.benkih.estore.product.repository.*;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.product.dto.response.ImageDto;
import com.benkih.estore.product.dto.request.AddProductRequest;
import com.benkih.estore.product.dto.request.UpdateProductRequest;
import com.benkih.estore.product.dto.response.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
  private final ProductVariantRepository productVariantRepository;
  private final CategoryAttributeRepository categoryAttributeRepository;
  private final AttributeValueRepository attributeValueRepository;


  @Transactional
  @Override
  public Product addProduct(AddProductRequest request, Long businessId) {

    if(productExists(request.getName(), request.getBrand(), businessId)){
      throw new AlreadyExistsException(
          request.getBrand() + " " + request.getName() + " already exists, you may update this product instead." );
    }

    Business business = businessRepository.findById(businessId)
        .orElseThrow(() ->
            new ResourceNotFoundException("Business not found")
        );
//    Category category = categoryRepository.findByName(request.getCategory())
//        .orElseThrow(() ->
//          new ResourceNotFoundException("Category not found"));
    Category category =
        categoryRepository.findByNameAndParentIsNull(request.getCategorySlug().trim())
        .orElseThrow(() ->
            new ResourceNotFoundException("Category not found"));

    Product product = createProduct(request, category, business);

//     populateAndValidateProductAttributes(
//         product,
//         request.getProductAttributes(),
//         category
//     );
    product = productRepository.save(product);

    for (CreateProductVariantRequest variantRequest : request.getVariants()) {
      ProductVariant variant = createVariant(
          variantRequest,
          product
      );

      populateAndValidateVariantAttributes(variant, variantRequest.getAttributes(), category);

      variant.setCombinationKey(buildCombinationKey(variant.getAttributes()));

      variant.setSku(generateSku(product, variant));

      productVariantRepository.save(variant);
      inventoryService.createInventory(variant);
    }

//    ProductVariant variant = createVariant(request, product, business);
//
//    variant.setSku(generateSku(product));
//    variant.setPrice(request.getPrice());
//    variant.setCurrency(Currency.NGN);
//    variant = productVariantRepository.save(variant);
//
//    inventoryService.createInventory(variant);

    return product;
  }

  private String buildCombinationKey(
      List<ProductVariantAttribute> attributes
  ) {
    if (attributes == null || attributes.isEmpty()) {
      return "default";
    }

    return attributes.stream()
        .sorted(Comparator.comparing(
            item -> item.getAttribute().getSlug()
        ))
        .map(item -> {
          String attributeSlug = item.getAttribute().getSlug();

          String value = item.getAttributeValue() != null
              ? item.getAttributeValue().getSlug()
              : item.getCustomValue();

          return attributeSlug + "=" + normalizeCombinationValue(value);
        })
        .collect(Collectors.joining("|"));
  }

  private String normalizeCombinationValue(String value) {
    if (value == null || value.isBlank()) {
      throw new BadRequestException(
          "A variant attribute value is required."
      );
    }

    return value
        .trim()
        .toLowerCase(Locale.ROOT)
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("(^-+|-+$)", "");
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
        category,
        business
    );
  }

  private ProductVariant createVariant(
      CreateProductVariantRequest variantRequest,
      Product product
  ) {

    ProductVariant variant = new ProductVariant();

    variant.setProduct(product);
    variant.setBusiness(product.getBusiness());
    variant.setPrice(variantRequest.getPrice());
    variant.setActive(true);

    return variant;
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



    List<ProductVariantResponseDto> variantDtos =
        Optional.ofNullable(product.getVariants())
            .orElse(List.of())
            .stream()
            .map(this::convertVariantToDto)
            .toList();

    return new ProductResponseDto(
        product.getSlug(),
//        product.getSku(),
        product.getStatus(),
        product.getName(),
        product.getBrand(),
        product.getDescription(),
//        product.getPrice(),
//        product.getInventory().getAvailableStock(),
//        product.getInventory().getAvailableStock() > 0,
        product.getCategory() != null ? product.getCategory().getName() : null,
        imageDtos,
        variantDtos
    );
  }

  private ProductVariantResponseDto convertVariantToDto(
      ProductVariant variant
  ) {

    Inventory inventory = variant.getInventory();

    List<VariantAttributeResponseDto> attributes =
        Optional.ofNullable(variant.getAttributes())
            .orElse(List.of())
            .stream()
            .map(attribute -> {

              String value =
                  attribute.getAttributeValue() != null
                      ? attribute.getAttributeValue().getValue()
                      : attribute.getCustomValue();

              return new VariantAttributeResponseDto(
                  attribute.getAttribute().getName(),
                  value
              );
            })
            .toList();


    return new ProductVariantResponseDto(
        variant.getSlug(),
        variant.getSku(),
        variant.getPrice(),
        variant.getCurrency(),
        variant.isActive(),
        inventory != null
            ? inventory.getAvailableStock()
            : 0,
        inventory != null &&
            inventory.getAvailableStock() > 0,
        attributes
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
     businessRepository.findById(businessId)
        .orElseThrow(() ->
            new ResourceNotFoundException("Business not found"));

    Product updatedProduct = productRepository.findBySlugAndBusinessId(slug, businessId)
        .map(existingProduct -> updateExistingProduct(existingProduct,
            request))
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

  private String generateSku(
      Product product,
      ProductVariant variant
  ) {
    long sequence = skuSequenceRepository.nextSkuNumber();

    String businessCode = skuToken(
        product.getBusiness().getName(),
        4
    );

    String productCode = skuToken(
        product.getBrand() + "-" + product.getName(),
        10
    );

    String variantCode = variantCode(variant);

    return String.format(
        "%s-%s-%s-%06d",
        businessCode,
        productCode,
        variantCode,
        sequence
    );
  }

  private String variantCode(ProductVariant variant) {
    if (variant.getAttributes() == null || variant.getAttributes().isEmpty()) {
      return "DEF";
    }

    return variant.getAttributes().stream()
        .sorted(Comparator.comparing(
            item -> item.getAttribute().getName()
        ))
        .map(item -> {
          String value = item.getAttributeValue() != null
              ? item.getAttributeValue().getValue()
              : item.getCustomValue();

          return skuToken(value, 6);
        })
        .collect(Collectors.joining("-"));
  }

  private String skuToken(String value, int maxLength) {
    if (value == null || value.isBlank()) {
      return "GEN";
    }

    String normalized = value
        .toUpperCase(Locale.ROOT)
        .replaceAll("[^A-Z0-9]", "");

    return normalized.substring(
        0,
        Math.min(normalized.length(), maxLength)
    );
  }


  private String abbreviate(String value) {
    String cleaned = value
        .replaceAll("[^A-Za-z]", "")
        .toUpperCase();

    return cleaned.substring(0, Math.min(3, cleaned.length()));
  }


  private Product updateExistingProduct(Product existingProduct,
                                        UpdateProductRequest request) {
    if (request.getName() != null) {
      existingProduct.setName(request.getName());
    }
    if (request.getBrand() != null) {
      existingProduct.setBrand(request.getBrand());
    }
    if (request.getDescription() != null) {
      existingProduct.setDescription(request.getDescription());
    }
    if (request.getCategoryName() != null) {
      Category category = categoryRepository.findByNameAndParentIsNull(request.getCategoryName().trim())
              .orElseThrow(() ->
                  new ResourceNotFoundException("Category not found"));
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

  private void validateProductCategory(Category category) {
    if (category == null) {
      throw new BadRequestException("A product category is required.");
    }

    if (!category.isActive()) {
      throw new BadRequestException("The selected category is inactive.");
    }

    if (categoryRepository.existsByParentId(category.getId())) {
      throw new BadRequestException(
          "Products must be assigned to a leaf category."
      );
    }
  }

  private void populateAndValidateVariantAttributes(
      ProductVariant variant,
      List<VariantAttributeRequest> requests,
      Category category
  ) {
    List<VariantAttributeRequest> attributeRequests =
        requests == null ? List.of() : requests;

    Set<String> requestedAttributeSlugs = new HashSet<>();

    for (VariantAttributeRequest request : attributeRequests) {
      String attributeSlug = request.getAttributeSlug().trim();

      if (!requestedAttributeSlugs.add(attributeSlug)) {
        throw new BadRequestException(
            "Duplicate variant attribute: " + attributeSlug
        );
      }

      CategoryAttribute attribute = categoryAttributeRepository
          .findByCategoryAndSlug(category, attributeSlug)
          .orElseThrow(() -> new BadRequestException(
              "Attribute '" + attributeSlug
                  + "' does not belong to category '"
                  + category.getName() + "'."
          ));

      if (!attribute.isActive()) {
        throw new BadRequestException(
            "Attribute '" + attribute.getName() + "' is inactive."
        );
      }

      if (!attribute.isVariantAttribute()) {
        throw new BadRequestException(
            "Attribute '" + attribute.getName()
                + "' cannot be used to create a product variant."
        );
      }

      ProductVariantAttribute variantAttribute =
          createVariantAttribute(variant, attribute, request);

      variant.getAttributes().add(variantAttribute);
    }

    validateRequiredVariantAttributes(
        category,
        requestedAttributeSlugs
    );
  }

  private ProductVariantAttribute createVariantAttribute(
      ProductVariant variant,
      CategoryAttribute attribute,
      VariantAttributeRequest request
  ) {
    boolean hasValueSlug =
        request.getAttributeValueSlug() != null
            && !request.getAttributeValueSlug().isBlank();

    boolean hasCustomValue =
        request.getCustomValue() != null
            && !request.getCustomValue().isBlank();

    if (hasValueSlug == hasCustomValue) {
      throw new BadRequestException(
          "Provide either attributeValueSlug or customValue for '"
              + attribute.getName() + "'."
      );
    }

    if (hasValueSlug) {
      AttributeValue attributeValue = attributeValueRepository
          .findByAttributeAndSlug(
              attribute,
              request.getAttributeValueSlug().trim()
          )
          .orElseThrow(() -> new BadRequestException(
              "'" + request.getAttributeValueSlug()
                  + "' is not a valid value for "
                  + attribute.getName() + "."
          ));

      if (!attributeValue.isActive()) {
        throw new BadRequestException(
            "The selected value for "
                + attribute.getName() + " is inactive."
        );
      }

      return new ProductVariantAttribute(
          variant,
          attribute,
          attributeValue
      );
    }

    if (attribute.getType() != AttributeType.TEXT) {
      throw new BadRequestException(
          "Custom values are not permitted for "
              + attribute.getName() + "."
      );
    }

    return new ProductVariantAttribute(
        variant,
        attribute,
        request.getCustomValue().trim()
    );
  }

  private void validateRequiredVariantAttributes(
      Category category,
      Set<String> requestedAttributeSlugs
  ) {
    List<CategoryAttribute> requiredAttributes =
        categoryAttributeRepository
            .findByCategoryAndActiveTrueAndVariantAttributeTrueAndRequiredTrue(
                category
            );

    for (CategoryAttribute attribute : requiredAttributes) {
      if (!requestedAttributeSlugs.contains(attribute.getSlug())) {
        throw new BadRequestException(
            "Variant attribute '" + attribute.getName()
                + "' is required."
        );
      }
    }
  }
}

