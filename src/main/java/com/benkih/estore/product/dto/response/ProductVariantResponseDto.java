package com.benkih.estore.product.dto.response;

import com.benkih.estore.common.enums.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProductVariantResponseDto {

  private String slug;
  private String sku;
  private BigDecimal price;
  private CurrencyCode currency;
  private boolean active;
  private Integer availableStock;
  private boolean inStock;
  private List<VariantAttributeResponseDto> attributes;
}
