package com.benkih.estore.email.builder;

import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.service.EmailTemplateService;
import com.benkih.estore.user.entity.User;

import java.util.HashMap;
import java.util.Map;

public class VerificationEmailBuilder {
  private final EmailTemplateService templateService;

  public VerificationEmailBuilder(EmailTemplateService templateService) {
    this.templateService = templateService;
  }

  public EmailRequest build(User user, String token) {

    Map<String, Object> vars = new HashMap<>();
    vars.put("name", user.getFirstName());
    vars.put("email", user.getEmail());
    vars.put("token",token);

    String html = templateService.render(
        "email/verifyEmail",
        vars
    );

    EmailRequest request = new EmailRequest();
    request.setTo(user.getEmail());
    request.setSubject("Welcome to E-Store");
    request.setHtml(html);

    return request;
  }
}
