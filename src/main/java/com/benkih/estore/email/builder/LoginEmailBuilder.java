package com.benkih.estore.email.builder;

import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.service.EmailTemplateService;
import com.benkih.estore.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class LoginEmailBuilder {
  private final EmailTemplateService templateService;

  public LoginEmailBuilder(EmailTemplateService templateService) {
    this.templateService = templateService;
  }

  public EmailRequest build(User user) {

    Map<String, Object> vars = new HashMap<>();
    vars.put("name", user.getFirstName());
    vars.put("email", user.getEmail());
    vars.put("loginTime", Instant.now());
    vars.put("ipAddress", "10.199.212.2");
    vars.put("device", "apple device mac");
    //  vars.put("ipAddress", ipAddress);
    //  vars.put("device", device);

    String html = templateService.render(
        "email/login",
        vars
    );

    EmailRequest request = new EmailRequest();
    request.setTo(user.getEmail());
    request.setSubject("Login to E-Store");
    request.setHtml(html);

    return request;
  }
}
