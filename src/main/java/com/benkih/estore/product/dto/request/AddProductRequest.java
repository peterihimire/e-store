package com.benkih.estore.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddProductRequest {
  @NotBlank(message = "Product name is required")
  private String name;
  @NotBlank(message = "Product brand is required")
  private String brand;
  @NotBlank(message = "Product description is required")
  private String description;

  @NotNull(message = "Product price is required")
  @DecimalMin(value = "0.01", message = "Price must be greater than zero")
  private BigDecimal price;

  @NotBlank(message = "Product category is required")
  private String category;
}
