package com.benkih.estore.pdf.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class OrderDocument {
  private String orderNumber;
  private LocalDateTime orderDate;
  private String customerName;
  private String customerEmail;
  private BigDecimal totalAmount;
  private String paymentMethod;
  private List<OrderItemDocument> items;
}
