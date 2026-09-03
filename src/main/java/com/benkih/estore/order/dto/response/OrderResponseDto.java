package com.benkih.estore.order.dto.response;

import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
  private String slug;

  private String userSlug;

  private Instant orderDate;

  private CurrencyCode currency;

  private BigDecimal subTotal;

  private BigDecimal discountAmount;

  private BigDecimal taxAmount;

  private BigDecimal shippingFee;

  private BigDecimal totalAmount;

  private OrderStatus status;

  private List<OrderItemResponseDto> items;
}
