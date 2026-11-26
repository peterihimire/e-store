package com.benkih.estore.product.service;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.business.repository.BusinessRepository;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.inventory.service.InventoryService;
import com.benkih.estore.product.dto.request.AddProductRequest;
import com.benkih.estore.product.dto.request.CreateProductVariantRequest;
import com.benkih.estore.product.dto.request.UpdateProductRequest;
import com.benkih.estore.product.dto.response.ProductResponseDto;
import com.benkih.estore.product.entity.Brand;
import com.benkih.estore.product.entity.Category;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.entity.ProductVariant;
import com.benkih.estore.product.repository.BrandRepository;
import com.benkih.estore.product.repository.CategoryRepository;
import com.benkih.estore.product.repository.ProductRepository;
import com.benkih.estore.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductMutationService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryService inventoryService;
    private final BusinessRepository businessRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BrandRepository brandRepository;
    private final ProductCatalogService productCatalogService;
    private final ProductAttributeService productAttributeService;
    private final ProductSkuService productSkuService;

    @Transactional
    public ProductResponseDto addProduct(AddProductRequest request, Long businessId) {

        Brand brand =
                brandRepository
                        .findBySlug(request.getBrandSlug())
                        .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        if (productExists(request.getName(), brand, businessId)) {
            throw new AlreadyExistsException(
                    brand.getName()
                            + " "
                            + request.getName()
                            + " already exists, you may "
                            + "update this product instead.");
        }

        Business business =
                businessRepository
                        .findById(businessId)
                        .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        Category category =
                categoryRepository
                        .findBySlug(request.getCategorySlug().trim())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = createProduct(request, category, business, brand);

        productAttributeService.populateAndValidateProductAttributes(
                product, request.getProductAttributes(), category);

        product = productRepository.save(product);

        for (CreateProductVariantRequest variantRequest : request.getVariants()) {
            ProductVariant variant = createVariant(variantRequest, product);

            productAttributeService.populateAndValidateVariantAttributes(
                    variant, variantRequest.getAttributes(), category);

            variant.setCombinationKey(
                    productAttributeService.buildCombinationKey(variant.getAttributes()));

            variant.setSku(productSkuService.generateSku(product, variant));

            productVariantRepository.save(variant);
            inventoryService.createInventory(variant);
        }
        return productCatalogService.convertToDto(product);
    }

    public boolean productExists(String name, Brand brand, Long businessId) {
        return productRepository.existsByNameAndBrandAndBusinessId(name, brand, businessId);
    }

    private Product createProduct(
            AddProductRequest request, Category category, Business business, Brand brand) {

        return new Product(request.getName(), brand, request.getDescription(), category, business);
    }

    private ProductVariant createVariant(
            CreateProductVariantRequest variantRequest, Product product) {

        ProductVariant variant = new ProductVariant();

        variant.setProduct(product);
        variant.setBusiness(product.getBusiness());
        variant.setPrice(variantRequest.getPrice());
        variant.setActive(true);

        return variant;
    }

    public ProductResponseDto updateProduct(
            UpdateProductRequest request, String slug, Long businessId) {
        businessRepository
                .findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        Product updatedProduct =
                productRepository
                        .findBySlugAndBusinessId(slug, businessId)
                        .map(existingProduct -> updateExistingProduct(existingProduct, request))
                        .map(productRepository::save)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));

        return productCatalogService.convertToDto(updatedProduct);
    }

    public Product updateExistingProduct(Product existingProduct, UpdateProductRequest request) {
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
            Category category =
                    categoryRepository
                            .findByNameAndParentIsNull(request.getCategoryName().trim())
                            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            existingProduct.setCategory(category);
        }

        return existingProduct;
    }

    public void deleteProductById(String slug, Long businessId) {
        productRepository
                .findBySlugAndBusinessId(slug, businessId)
                .ifPresentOrElse(
                        productRepository::delete,
                        () -> {
                            throw new ResourceNotFoundException("Product not found!");
                        });
    }

    public void validateProductCategory(Category category) {
        if (category == null) {
            throw new BadRequestException("A product category is required.");
        }

        if (!category.isActive()) {
            throw new BadRequestException("The selected category is inactive.");
        }

        if (categoryRepository.existsByParentId(category.getId())) {
            throw new BadRequestException("Products must be assigned to a leaf category.");
        }
    }
}
