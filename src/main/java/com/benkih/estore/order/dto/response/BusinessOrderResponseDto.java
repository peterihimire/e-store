package com.benkih.estore.order.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessOrderResponseDto {

  private String orderSlug;
  private String orderNumber;

  private LocalDateTime orderDate;

  private String orderStatus;
  private String paymentStatus;

  private List<BusinessOrderItemResponseDto> items;

  private BigDecimal subtotal;
  private BigDecimal total;
}
