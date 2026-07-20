package com.benkih.estore.pdf.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data

@Builder
public class ReceiptItemDocument {
  private String productName;

  private Integer quantity;

  private BigDecimal unitPrice;

  private BigDecimal totalPrice;
}
