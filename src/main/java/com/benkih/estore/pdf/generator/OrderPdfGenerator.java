package com.benkih.estore.pdf.generator;

import com.benkih.estore.common.enums.OrderStatus;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import static com.benkih.estore.pdf.common.PdfConstants.*;
import static com.benkih.estore.pdf.common.PdfFormatter.formatDate;
import static com.benkih.estore.pdf.common.PdfFormatter.formatMoney;

@Service
public class OrderPdfGenerator {

  public byte[] generate(Order order) {

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Document pdf = new Document(PageSize.A4, 40, 40, 40, 40);
      PdfWriter.getInstance(pdf, out);

      pdf.open();

      addHeader(pdf, order);
      addWelcome(pdf, order);
      addOrderInformation(pdf, order);
      addItems(pdf, order);
      addTotal(pdf, order);
      addNextSteps(pdf, order);
      addFooter(pdf);

      pdf.close();

      return out.toByteArray();

    } catch (Exception ex) {
      throw new RuntimeException("Unable to generate order confirmation.", ex);
    }
  }

  private void addHeader(Document pdf, Order order) throws Exception {
    Paragraph company = new Paragraph(COMPANY_NAME, TITLE_FONT);
    company.setAlignment(Element.ALIGN_CENTER);
    pdf.add(company);
    Paragraph title = new Paragraph(getOrderTitle(order.getOrderStatus()), HEADER_FONT);
    title.setAlignment(Element.ALIGN_CENTER);
    pdf.add(title);
    pdf.add(Chunk.NEWLINE);
  }

  private void addWelcome(Document pdf, Order order) throws Exception {

    pdf.add(new Paragraph(
        "Hello " + order.getUser().getFirstName() + ",",
        HEADER_FONT));

    pdf.add(new Paragraph(" "));

    pdf.add(new Paragraph(
        "Thank you for shopping with " + COMPANY_NAME + ".",
        BODY_FONT));

    pdf.add(new Paragraph(
        getStatusMessage(order.getOrderStatus()),
        BODY_FONT
    ));

//    pdf.add(new Paragraph(
//        "Your order has been confirmed and is now being prepared.",
//        BODY_FONT));

    pdf.add(Chunk.NEWLINE);
  }

  private void addOrderInformation(Document pdf, Order order)
      throws Exception {

    Paragraph heading =
        new Paragraph("ORDER INFORMATION", HEADER_FONT);

    pdf.add(heading);

    PdfPTable table = new PdfPTable(2);

    table.setWidthPercentage(100);

    table.setSpacingAfter(20);

    table.setWidths(new float[]{2,3});

    table.addCell(labelCell("Order Number"));
    table.addCell(valueCell(order.getOrderNumber()));

    table.addCell(labelCell("Order Date"));
    table.addCell(valueCell(formatDate(order.getCreatedAt())));

    table.addCell(labelCell("Status"));
    table.addCell(valueCell(order.getOrderStatus().name()));

    pdf.add(table);
  }

  private void addItems(Document pdf, Order order)
      throws Exception {

    Paragraph heading =
        new Paragraph("ITEMS PURCHASED", HEADER_FONT);

    pdf.add(heading);

    PdfPTable table = new PdfPTable(4);

    table.setWidthPercentage(100);

    table.setSpacingBefore(10);

    table.setSpacingAfter(20);

    table.setWidths(new float[]{5,1,2,2});

    table.addCell(headerCell("Product"));
    table.addCell(headerCell("Qty"));
    table.addCell(headerCell("Price"));
    table.addCell(headerCell("Total"));

    for (OrderItem item : order.getOrderItems()) {

      BigDecimal total =
          item.getVariant().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

      table.addCell(bodyCell(item.getProduct().getName()));
      table.addCell(bodyCell(String.valueOf(item.getQuantity())));
      table.addCell(bodyCell(formatMoney(item.getVariant().getPrice())));
      table.addCell(bodyCell(formatMoney(total)));
    }

    pdf.add(table);
  }

  private void addTotal(Document pdf, Order order)
      throws Exception {

    PdfPTable table = new PdfPTable(2);

    table.setHorizontalAlignment(Element.ALIGN_RIGHT);

    table.setTotalWidth(220);

    table.setLockedWidth(true);

    table.addCell(labelCell("TOTAL"));

    table.addCell(valueCell(formatMoney(order.getTotalAmount())));

    pdf.add(table);

    pdf.add(Chunk.NEWLINE);
  }

  private void addNextSteps(Document pdf)
      throws Exception {

    Paragraph heading =
        new Paragraph("WHAT HAPPENS NEXT?", HEADER_FONT);

    pdf.add(heading);

    pdf.add(new Paragraph(""));

    pdf.add(new Paragraph("• Your order has been confirmed.", BODY_FONT));

    pdf.add(new Paragraph("• We'll begin preparing your order.", BODY_FONT));

    pdf.add(new Paragraph("• You'll receive another email once your order has been shipped.", BODY_FONT));

    pdf.add(Chunk.NEWLINE);
  }

  private void addFooter(Document pdf)
      throws Exception {

    Paragraph footer =
        new Paragraph(
            "Need help?\n\n" +
                "support@estore.com\n" +
                "www.estore.com\n\n" +
                "Thank you for shopping with us!",
            BODY_FONT);

    footer.setAlignment(Element.ALIGN_CENTER);

    footer.setSpacingBefore(30);

    pdf.add(footer);
  }

  private PdfPCell headerCell(String text) {

    PdfPCell cell =
        new PdfPCell(new Phrase(text, HEADER_FONT));

    cell.setBackgroundColor(PRIMARY);

    cell.setBorderColor(BORDER);

    cell.setHorizontalAlignment(Element.ALIGN_CENTER);

    cell.setPadding(8);

    return cell;
  }

  private PdfPCell bodyCell(String text) {

    PdfPCell cell =
        new PdfPCell(new Phrase(text, BODY_FONT));

    cell.setBorderColor(BORDER);

    cell.setPadding(8);

    return cell;
  }

  private PdfPCell labelCell(String text) {

    PdfPCell cell =
        new PdfPCell(new Phrase(text, LABEL_FONT));

    cell.setBackgroundColor(LIGHT_GRAY);

    cell.setBorderColor(BORDER);

    cell.setPadding(7);

    return cell;
  }

  private PdfPCell valueCell(String text) {

    PdfPCell cell =
        new PdfPCell(new Phrase(text, BODY_FONT));

    cell.setBorderColor(BORDER);

    cell.setPadding(7);

    return cell;
  }

  private String getStatusMessage(OrderStatus status) {
    return switch (status) {
      case CONFIRMED ->
          "Your order has been confirmed and is now being prepared.";

      case PROCESSING ->
          "Your order is currently being processed.";

      case SHIPPED ->
          "Your order has been shipped.";

      case DELIVERED ->
          "Your order has been delivered.";

      case CANCELLED ->
          "Your order has been cancelled.";

      default ->
          "Thank you for shopping with us.";
    };
  }

  private void addNextSteps(Document pdf, Order order)
      throws Exception {

    Paragraph heading =
        new Paragraph("WHAT HAPPENS NEXT?", HEADER_FONT);

    pdf.add(heading);
    pdf.add(new Paragraph(""));

    switch (order.getOrderStatus()) {

      case CONFIRMED -> {
        pdf.add(new Paragraph("• Your order has been confirmed.", BODY_FONT));
        pdf.add(new Paragraph("• Our team will begin preparing your order.", BODY_FONT));
        pdf.add(new Paragraph("• You'll receive another email when your order is being processed.", BODY_FONT));
      }

      case PROCESSING -> {
        pdf.add(new Paragraph("• Your order is currently being prepared.", BODY_FONT));
        pdf.add(new Paragraph("• Once it's ready, we'll ship it to your delivery address.", BODY_FONT));
        pdf.add(new Paragraph("• You'll receive another email when your order has been shipped.", BODY_FONT));
      }

      case SHIPPED -> {
        pdf.add(new Paragraph("• Your order is on its way.", BODY_FONT));
        pdf.add(new Paragraph("• Please monitor the tracking information provided.", BODY_FONT));
        pdf.add(new Paragraph("• We'll notify you once your package has been delivered.", BODY_FONT));
      }

      case DELIVERED -> {
        pdf.add(new Paragraph("• Your order has been successfully delivered.", BODY_FONT));
        pdf.add(new Paragraph("• We hope you enjoy your purchase.", BODY_FONT));
        pdf.add(new Paragraph("• Contact our support team if you need any assistance.", BODY_FONT));
      }

      case CANCELLED -> {
        pdf.add(new Paragraph("• Your order has been cancelled.", BODY_FONT));
        pdf.add(new Paragraph("• If payment was already received, your refund will be processed shortly.", BODY_FONT));
      }

      default -> {
        pdf.add(new Paragraph("Thank you for shopping with E-Store.", BODY_FONT));
      }
    }

    pdf.add(Chunk.NEWLINE);
  }

  private String getOrderTitle(OrderStatus status) {
    return switch (status) {
      case CONFIRMED -> "ORDER CONFIRMATION";
      case PROCESSING -> "ORDER PROCESSING";
      case SHIPPED -> "ORDER SHIPPED";
      case DELIVERED -> "ORDER DELIVERED";
      case CANCELLED -> "ORDER CANCELLED";
      case RETURN_REQUESTED -> "RETURN REQUEST RECEIVED";
      case RETURNED -> "ORDER RETURNED";
      case PENDING -> "ORDER PENDING";
      case EXPIRED -> "ORDER EXPIRED";
    };
  }
}