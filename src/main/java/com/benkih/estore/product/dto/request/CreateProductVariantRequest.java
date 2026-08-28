package com.benkih.estore.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CreateProductVariantRequest {

  // Optional: backend generates one when absent.
  @Size(max = 100)
  private String sku;

  @NotNull(message = "Variant price is required")
  @DecimalMin(value = "0.01", message = "Price must be greater than zero")
  private BigDecimal price;

  @Valid
  private List<VariantAttributeRequest> attributes = new ArrayList<>();
}
