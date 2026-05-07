package com.benkih.estore.product.dto.request;

import com.benkih.estore.category.entity.Category;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddProductRequest {
  private Long id;
  private String slug;
  private String brand;
  private String name;
  private String description;
  private BigDecimal price;
  private int inventory;
  private Category category;
}
