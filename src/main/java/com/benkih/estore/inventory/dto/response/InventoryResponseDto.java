package com.benkih.estore.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryResponseDto {
  private String slug;

  private Integer totalStock;

  private Integer availableStock;

  private Integer reservedStock;

  private Integer damagedStock;

  private Boolean inStock;

  private Boolean needsReorder;

  private Integer reorderLevel;

  private Integer reorderQuantity;

  private String productName;
}
