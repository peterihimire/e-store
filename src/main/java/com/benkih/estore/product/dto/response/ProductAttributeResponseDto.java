package com.benkih.estore.product.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductAttributeResponseDto {
  private String attributeSlug;

  private String attributeName;

  private String attributeValueSlug;

  private String value;
}
