package com.benkih.estore.inventory.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class InventoryAdjustmentRequest {
  @Min(1)
  private Integer quantity;
}
