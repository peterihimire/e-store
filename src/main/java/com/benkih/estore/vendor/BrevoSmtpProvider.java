package com.benkih.estore.vendor;

import com.benkih.estore.common.exceptions.EmailException;
import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.provider.AbstractEmailProvider;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.email.provider", havingValue = "brevo")
public class BrevoSmtpProvider extends AbstractEmailProvider {
    private final JavaMailSender mailSender;

    public BrevoSmtpProvider(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public String send(EmailRequest request) {
        validate(request);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getHtml(), true);

            mailSender.send(mimeMessage);
            return mimeMessage.getMessageID();

        } catch (MessagingException e) {
            throw new EmailException("Failed to compose email", e);
        }
    }
}
