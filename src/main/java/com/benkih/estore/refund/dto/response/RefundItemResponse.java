package com.benkih.estore.refund.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
