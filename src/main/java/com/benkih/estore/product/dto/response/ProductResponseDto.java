package com.benkih.estore.product.dto.response;

import com.benkih.estore.image.dto.ImageDto;
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
  private String name;
  private String brand;
  private String description;
  private BigDecimal price;
  private int inventory;

  private String category;

  private List<ImageDto> images;
}
