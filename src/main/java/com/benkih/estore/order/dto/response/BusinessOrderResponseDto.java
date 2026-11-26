package com.benkih.estore.order.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessOrderResponseDto {

    private String orderSlug;
    private String orderNumber;

    private Instant orderDate;

    private String orderStatus;
    private String paymentStatus;

    private List<BusinessOrderItemResponseDto> items;

    private BigDecimal subtotal;
    private BigDecimal total;
}
