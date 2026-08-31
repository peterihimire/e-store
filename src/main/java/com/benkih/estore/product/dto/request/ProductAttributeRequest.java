package com.benkih.estore.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductAttributeRequest {

  @NotBlank(message = "Attribute is required")
  private String attributeSlug;

  // Used for SELECT-type attributes.
  private String attributeValueSlug;

  // Used only for allowed TEXT-type attributes.
  @Size(max = 255)
  private String customValue;
}
