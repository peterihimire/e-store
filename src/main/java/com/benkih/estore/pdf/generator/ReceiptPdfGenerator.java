package com.benkih.estore.pdf.generator;

import com.benkih.estore.order.entity.OrderItem;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.pdf.model.ReceiptDocument;
import com.benkih.estore.pdf.model.ReceiptItemDocument;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import static com.benkih.estore.pdf.common.PdfConstants.*;
import static com.benkih.estore.pdf.common.PdfFormatter.formatDate;
import static com.benkih.estore.pdf.common.PdfFormatter.formatMoney;

import com.benkih.estore.pdf.common.PdfFormatter;

@Service
public class ReceiptPdfGenerator {

  public byte[] generate(Payment payment) {
    ReceiptDocument receipt = buildReceiptDocument(payment);

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Document pdf = new Document(PageSize.A4, 40, 40, 40, 40);
      PdfWriter.getInstance(pdf, out);

      pdf.open();

      addCompanyHeader(pdf);
      addReceiptInformation(pdf, receipt);
      addCustomerSection(pdf, receipt);
      addItemsTable(pdf, receipt);
      addSummary(pdf, receipt);
      addFooter(pdf);

      pdf.close();

      return out.toByteArray();

    } catch (Exception e) {
      throw new RuntimeException("Unable to generate receipt.", e);
    }
  }

  private void addCompanyHeader(Document pdf) throws DocumentException {
    Paragraph company = new Paragraph(COMPANY_NAME, TITLE_FONT);
    company.setAlignment(Element.ALIGN_CENTER);
    pdf.add(company);

    Paragraph subtitle = new Paragraph("PAYMENT RECEIPT", HEADER_FONT);
    subtitle.setAlignment(Element.ALIGN_CENTER);
    pdf.add(subtitle);
    pdf.add(Chunk.NEWLINE);
  }

  private void addReceiptInformation(Document pdf, ReceiptDocument receipt) throws DocumentException {
    PdfPTable table = new PdfPTable(2);

    table.setWidthPercentage(100);
    table.setSpacingAfter(20);
    table.setWidths(new float[]{2,3});

    table.addCell(labelCell("Receipt Number"));
    table.addCell(valueCell(receipt.getReceiptNumber()));

    table.addCell(labelCell("Reference"));
    table.addCell(valueCell(receipt.getPaymentReference()));

    table.addCell(labelCell("Order"));
    table.addCell(valueCell(receipt.getOrderNumber()));

    table.addCell(labelCell("Payment Method"));
    table.addCell(valueCell(receipt.getPaymentMethod()));

    table.addCell(labelCell("Payment Date"));
    table.addCell(valueCell(formatDate(receipt.getPaymentDate())));

    pdf.add(table);
  }

  private void addCustomerSection(Document pdf, ReceiptDocument receipt) throws DocumentException {
    Paragraph heading = new Paragraph("Customer Information", HEADER_FONT);
    pdf.add(heading);
    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setSpacingAfter(20);
    table.setWidths(new float[]{2,3});

    table.addCell(labelCell("Name"));
    table.addCell(valueCell(receipt.getCustomerName()));

    table.addCell(labelCell("Email"));
    table.addCell(valueCell(receipt.getCustomerEmail()));

    pdf.add(table);
  }

  private void addSummary(Document pdf, ReceiptDocument receipt) throws DocumentException {
    PdfPTable summary = new PdfPTable(2);
    summary.setHorizontalAlignment(Element.ALIGN_RIGHT);
    summary.setTotalWidth(220);
    summary.setLockedWidth(true);
    summary.addCell(labelCell("TOTAL"));
    summary.addCell(valueCell(formatMoney(receipt.getAmount())));
    pdf.add(summary);

    pdf.add(Chunk.NEWLINE);
  }

  private void addFooter(Document pdf) throws DocumentException {

    Paragraph footer = new Paragraph(
        "Thank you for your purchase.\n\n" +
            "Benkih Store\n" +
            "support@benkih.com\n" +
            "www.benkih.com",
        BODY_FONT
    );

    footer.setAlignment(Element.ALIGN_CENTER);
    footer.setSpacingBefore(30);
    pdf.add(footer);
  }

  private PdfPCell labelCell(String text) {
    PdfPCell cell = new PdfPCell(new Phrase(text, LABEL_FONT));
    cell.setBorderColor(BORDER);
    cell.setBackgroundColor(LIGHT_GRAY);
    cell.setPadding(7);

    return cell;
  }

  private PdfPCell valueCell(String text) {
    PdfPCell cell = new PdfPCell(new Phrase(text, BODY_FONT));
    cell.setBorderColor(BORDER);
    cell.setPadding(7);

    return cell;
  }


  private void addItemsTable(Document pdf, ReceiptDocument receipt) throws DocumentException {
    PdfPTable table = new PdfPTable(4);

    table.setWidthPercentage(100);
    table.setSpacingBefore(10f);
    table.setSpacingAfter(20f);

    table.setWidths(new float[]{5f, 1f, 2f, 2f});

    table.addCell(createHeaderCell("Product", HEADER_FONT));
    table.addCell(createHeaderCell("Qty", HEADER_FONT));
    table.addCell(createHeaderCell("Unit Price", HEADER_FONT));
    table.addCell(createHeaderCell("Total", HEADER_FONT));

    for (ReceiptItemDocument item : receipt.getItems()) {
      table.addCell(createCell(item.getProductName(), BODY_FONT));
      table.addCell(createCell(String.valueOf(item.getQuantity()), BODY_FONT));
      table.addCell(createCell(formatMoney(item.getUnitPrice()), BODY_FONT));
      table.addCell(createCell(formatMoney(item.getTotalPrice()), BODY_FONT));
    }

    pdf.add(table);
  }


  private ReceiptDocument buildReceiptDocument(Payment payment) {

    List<ReceiptItemDocument> items =
        payment.getOrder()
            .getOrderItems()
            .stream()
            .map(this::convertItem)
            .toList();

    return ReceiptDocument.builder()
        .receiptNumber(payment.getReference())
        .paymentReference(payment.getReference())
        .customerName(payment.getUser().getFullName())
        .customerEmail(payment.getUser().getEmail())
        .amount(payment.getAmount())
        .paymentDate(payment.getPaidAt())
        .paymentMethod(payment.getPaymentMethod().name())
        .orderNumber(payment.getOrder().getOrderNumber())
        .items(items)
        .build();
  }

  private ReceiptItemDocument convertItem(OrderItem item) {
    return ReceiptItemDocument.builder()
        .productName(item.getProduct().getName())
        .quantity(item.getQuantity())
        .unitPrice(item.getProduct().getPrice())
        .totalPrice(
            item.getProduct()
                .getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
        )
        .build();
  }

  private PdfPCell createHeaderCell(String text, Font font) {
    PdfPCell cell = new PdfPCell(new Phrase(text, font));

    cell.setBackgroundColor(PRIMARY);
    cell.setBorderColor(BORDER);
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setPadding(8);

    return cell;
  }

  private PdfPCell createCell(String text, Font font) {
    PdfPCell cell = new PdfPCell(new Phrase(text, font));

    cell.setBorderColor(BORDER);
    cell.setPadding(8);

    return cell;
  }
}

//package com.benkih.estore.pdf.generator;
//
//import com.benkih.estore.payment.entity.Payment;
//import com.benkih.estore.pdf.model.ReceiptDocument;
//
//public class ReceiptPdfGenerator {
//
//  public byte[] generate(Payment payment) {
//    ReceiptDocument document = convertToDocument(payment);
//  }
//
//  private ReceiptDocument convertToDocument(Payment payment) {
//  }
//}
//

//  public byte[] generate(Payment payment) {
//
//    ReceiptDocument receipt = convertToDocument(payment);
//
//    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//
//      Document pdf = new Document();
//
//      PdfWriter.getInstance(pdf, outputStream);
//
//      pdf.open();
//
//      pdf.add(new Paragraph(
//          "PAYMENT RECEIPT",TITLE_FONT
////  FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)
//      ));
//
//      pdf.add(new Paragraph(" "));
//      pdf.add(new Paragraph("Receipt Number : " + receipt.getReceiptNumber()));
//      pdf.add(new Paragraph("Payment Reference : " + receipt.getPaymentReference()));
//      pdf.add(new Paragraph("Customer : " + receipt.getCustomerName()));
//      pdf.add(new Paragraph("Email : " + receipt.getCustomerEmail()));
//      pdf.add(new Paragraph("Order : " + receipt.getOrderNumber()));
//      pdf.add(new Paragraph("Payment Method : " + receipt.getPaymentMethod()));
//      pdf.add(new Paragraph("Payment Date : " + receipt.getPaymentDate()));
//      pdf.add(new Paragraph("Amount : " + receipt.getAmount()));
//
//      pdf.add(new Paragraph(" "));
//
//      Font tableHeaderFont =
//          FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
//
//      Font tableFont =
//          FontFactory.getFont(FontFactory.HELVETICA, 10);
//
//      PdfPTable table = new PdfPTable(4);
//      table.setWidthPercentage(100);
//      table.setSpacingBefore(10f);
//      table.setSpacingAfter(15f);
//
//      table.setWidths(new float[]{5f, 1f, 2f, 2f});
//
//      table.addCell(createHeaderCell("Product", tableHeaderFont));
//      table.addCell(createHeaderCell("Qty", tableHeaderFont));
//      table.addCell(createHeaderCell("Unit Price", tableHeaderFont));
//      table.addCell(createHeaderCell("Total", tableHeaderFont));
//
//      for (ReceiptItemDocument item : receipt.getItems()) {
//
//        table.addCell(createCell(item.getProductName(), tableFont));
//        table.addCell(createCell(String.valueOf(item.getQuantity()), tableFont));
//        table.addCell(createCell(item.getUnitPrice().toString(), tableFont));
//        table.addCell(createCell(item.getTotalPrice().toString(), tableFont));
//      }
//
//      pdf.add(table);
////      pdf.add(new Paragraph("Items"));
////
////      for (ReceiptItemDocument item : receipt.getItems()) {
////
////        pdf.add(new Paragraph(
////            String.format(
////                "%s  | Qty: %d | Unit: %s | Total: %s",
////                item.getProductName(),
////                item.getQuantity(),
////                item.getUnitPrice(),
////                item.getTotalPrice()
////            )
////        ));
////      }
//
//      pdf.close();
//
//      return outputStream.toByteArray();
//
//    } catch (DocumentException e) {
//      throw new RuntimeException("Unable to generate payment receipt.", e);
//    } catch (Exception e) {
//      throw new RuntimeException(e);
//    }
//  }
