package com.benkih.estore.refund.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundItemRequest {
  private String orderItemSlug;

  private Integer quantity;
}
