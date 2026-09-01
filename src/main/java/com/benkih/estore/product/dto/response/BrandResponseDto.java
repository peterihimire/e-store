package com.benkih.estore.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandResponseDto {

  private String slug;
  private String name;
  private String logoUrl;
  private String websiteUrl;
}
