package com.benkih.estore.email.builder;

import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.service.EmailTemplateService;
import com.benkih.estore.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PasswordResetEmailBuilder {
  private final EmailTemplateService templateService;

  public PasswordResetEmailBuilder(EmailTemplateService templateService) {
    this.templateService = templateService;
  }

  public EmailRequest build(User user, String token) {

    Map<String, Object> vars = new HashMap<>();
    vars.put("name", user.getFirstName());
    vars.put("token",token);

    String html = templateService.render(
        "email/passwordReset",
        vars
    );

    EmailRequest request = new EmailRequest();
    request.setTo(user.getEmail());
    request.setSubject("Reset your E-Store password");
    request.setHtml(html);

    return request;
  }
}
