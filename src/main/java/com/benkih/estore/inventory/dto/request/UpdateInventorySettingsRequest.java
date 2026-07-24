package com.benkih.estore.inventory.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateInventorySettingsRequest {

  @Min(0)
  private Integer reorderLevel;
  @Min(1)
  private Integer reorderQuantity;

}
