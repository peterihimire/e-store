package com.benkih.estore.refund.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundItemRequest {
  @NotBlank(message = "Order item slug is required.")
  private String orderItemSlug;

  @NotNull(message = "Refund quantity is required.")
  @Min(value = 1, message = "Refund quantity must be at least 1.")
  private Integer quantity;
}
