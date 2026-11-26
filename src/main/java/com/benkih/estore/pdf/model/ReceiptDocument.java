package com.benkih.estore.pdf.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReceiptDocument {

    private String receiptNumber;

    private String paymentReference;

    private String customerName;

    private String customerEmail;

    private BigDecimal amount;

    private Instant paymentDate;

    private String orderNumber;

    private String paymentMethod;

    private List<ReceiptItemDocument> items;
}
