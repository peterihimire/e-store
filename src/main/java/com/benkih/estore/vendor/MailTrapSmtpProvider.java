package com.benkih.estore.vendor;

import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.provider.AbstractEmailProvider;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.email.provider", havingValue = "mailtrap", matchIfMissing = true)
public class MailTrapSmtpProvider extends AbstractEmailProvider {
  private final JavaMailSender mailSender;

  public MailTrapSmtpProvider(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Override
  public void send(EmailRequest request) {
    validate(request);

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(request.getTo());
      helper.setSubject(request.getSubject());
      helper.setText(request.getHtml(), true);

      mailSender.send(message);

    } catch (MessagingException e) {
      throw new RuntimeException("Failed to send email using Mailtrap", e);
    }
  }
}