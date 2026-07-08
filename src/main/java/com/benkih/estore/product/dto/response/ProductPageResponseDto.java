package com.benkih.estore.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPageResponseDto {
  private List<ProductResponseDto> products;
  private long total;
  private int totalPages;
  private int page;
  private int limit;
}
