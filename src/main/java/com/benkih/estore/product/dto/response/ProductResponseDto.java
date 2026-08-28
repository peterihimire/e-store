package com.benkih.estore.product.dto.response;

import com.benkih.estore.common.enums.ProductStatus;
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
//  private String sku;
  private ProductStatus status;
  private String name;
  private String brand;
  private String description;
//  private BigDecimal price;
//  private int inventory;
//  private Integer availableStock;
//  private boolean inStock;
  private String category;
  private List<ImageDto> images;
  List<ProductVariantResponseDto> variants;
}
