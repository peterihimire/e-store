package com.benkih.estore.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VariantAttributeRequest {

  @NotBlank(message = "Attribute is required")
  private String attributeSlug;

  // Required for SELECT-type attributes.
  private String attributeValueSlug;

  // Used only for permitted free-text attributes.
  @Size(max = 255)
  private String customValue;
}
