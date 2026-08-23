package com.benkih.estore.order.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessOrderItemResponseDto {

  private String orderItemSlug;
  private String orderSlug;
  private String orderNumber;

  private String productSlug;
  private String productName;
  private String productSku;
  private String productBrand;

  private int quantity;
  private BigDecimal price;
  private BigDecimal total;

  private String orderStatus;
  private String paymentStatus;

  private LocalDateTime orderDate;
}