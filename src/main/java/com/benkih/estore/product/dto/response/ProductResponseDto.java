package com.benkih.estore.product.dto.response;

import com.benkih.estore.common.enums.ProductStatus;
import com.benkih.estore.product.entity.Brand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {
  private String slug;
  private ProductStatus status;
  private String name;
  private String description;
  private String category;
  private List<ImageDto> images;
  List<ProductVariantResponseDto> variants;
  private BrandResponseDto brand;
}
