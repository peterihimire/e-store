package com.benkih.estore.email.builder;

import com.benkih.estore.email.dto.EmailAttachment;
import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.service.EmailTemplateService;
import com.benkih.estore.payment.entity.Payment;
import com.benkih.estore.pdf.generator.ReceiptPdfGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentReceiptEmailBuilder {

  private final EmailTemplateService templateService;
  private final ReceiptPdfGenerator receiptPdfGenerator;

  public EmailRequest build(Payment payment) {

    Map<String, Object> vars = new HashMap<>();

    vars.put("name", payment.getUser().getFirstName());
    vars.put("amount", payment.getAmount());
    vars.put("reference", payment.getReference());
    vars.put("orderNumber", payment.getOrder().getSlug());
    vars.put("paymentDate", payment.getPaidAt());

    String html = templateService.render(
        "email/paymentReceipt",
        vars
    );

    byte[] pdf = receiptPdfGenerator.generate(payment);

    EmailAttachment attachment = new EmailAttachment();
    attachment.setFilename("Receipt-" + payment.getReference() + ".pdf");
    attachment.setContentType("application/pdf");
    attachment.setContent(pdf);

    EmailRequest request = new EmailRequest();
    request.setTo(payment.getUser().getEmail());
    request.setSubject("Payment Receipt");
    request.setHtml(html);
    request.setAttachments(List.of(attachment));

    return request;
  }
}
// @Builder was not used
//    EmailAttachment attachment =
//        EmailAttachment.builder()
//            .filename("Receipt-" + payment.getReference() + ".pdf")
//            .contentType("application/pdf")
//            .content(pdf)
//            .build();