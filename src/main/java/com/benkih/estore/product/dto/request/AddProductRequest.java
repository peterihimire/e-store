package com.benkih.estore.product.dto.request;

import com.benkih.estore.common.enums.CurrencyCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Data
public class AddProductRequest {

  @NotBlank(message = "Product name is required")
  private String name;

  @NotBlank(message = "Product brand is required")
  private String brand;

  @NotBlank(message = "Product description is required")
  private String description;

  @NotBlank(message = "Product category is required")
  private String categorySlug;

  @NotNull(message = "Currency is required")
  private CurrencyCode currency = CurrencyCode.NGN;

  @Valid
  private List<ProductAttributeRequest> productAttributes = new ArrayList<>();

  @NotEmpty(message = "At least one product variant is required")
  @Valid
  private List<CreateProductVariantRequest> variants;
}
//@Data
//public class AddProductRequest {
//  @NotBlank(message = "Product name is required")
//  private String name;
//  @NotBlank(message = "Product brand is required")
//  private String brand;
//  @NotBlank(message = "Product description is required")
//  private String description;
//
//  @NotNull(message = "Product price is required")
//  @DecimalMin(value = "0.01", message = "Price must be greater than zero")
//  private BigDecimal price;
//
//  @NotBlank(message = "Product category is required")
//  private String category;
//}
