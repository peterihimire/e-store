package com.benkih.estore.refund.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundItemResponse {

  private String slug;

  private String orderItemSlug;

  private Integer quantity;

  private BigDecimal amount;

}
