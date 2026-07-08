package com.benkih.estore.email.builder;

import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.service.EmailTemplateService;
import com.benkih.estore.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WelcomeEmailBuilder {
  private final EmailTemplateService templateService;

  public WelcomeEmailBuilder(EmailTemplateService templateService) {
    this.templateService = templateService;
  }

  public EmailRequest build(User user) {

    Map<String, Object> vars = new HashMap<>();
    vars.put("name", user.getFirstName());
    vars.put("email", user.getEmail());

    String html = templateService.render(
        "email/welcome",
        vars
    );

    EmailRequest request = new EmailRequest();
    request.setTo(user.getEmail());
    request.setSubject("Welcome to E-Store");
    request.setHtml(html);

    return request;
  }
}
