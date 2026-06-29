package com.benkih.estore.product.dto.request;

import jakarta.validation.constraints.NotBlank;
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

  private BigDecimal price;
  private int inventory;
  private String category;
}
