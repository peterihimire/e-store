package com.benkih.estore.pdf.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data

@Builder

public class ReceiptDocument {

  private String receiptNumber;

  private String paymentReference;

  private String customerName;

  private String customerEmail;

  private BigDecimal amount;

  private LocalDateTime paymentDate;

  private String orderNumber;

  private String paymentMethod;

  private List<ReceiptItemDocument> items;

}
