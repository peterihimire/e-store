package com.benkih.estore.email.builder;

import com.benkih.estore.email.dto.EmailAttachment;
import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.service.EmailTemplateService;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.pdf.generator.OrderPdfGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderShippedEmailBuilder {
  private final EmailTemplateService templateService;
  private final OrderPdfGenerator orderPdfGenerator;

  public EmailRequest build(Order order) {

    Map<String, Object> vars = new HashMap<>();

    vars.put("name", order.getUser().getFirstName());
    vars.put("orderNumber", order.getSlug());
    vars.put("orderDate", order.getCreatedAt());
    vars.put("status", order.getOrderStatus().name());
    vars.put("total", order.getTotalAmount());
    vars.put("items", order.getOrderItems());

    String html = templateService.render(
        "email/orderShipped",
        vars
    );

    byte[] pdf = orderPdfGenerator.generate(order);

    EmailAttachment attachment = new EmailAttachment();
    attachment.setFilename("Shipped-" + order.getOrderNumber()+ ".pdf");
    attachment.setContentType("application/pdf");
    attachment.setContent(pdf);

    EmailRequest request = new EmailRequest();
    request.setTo(order.getUser().getEmail());
    request.setSubject("Order Shipped");
    request.setHtml(html);
    request.setAttachments(List.of(attachment));

    return request;
  }

}
