package com.benkih.estore.email.builder;


import com.benkih.estore.auth.entity.UserInvitation;
import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.service.EmailTemplateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InvitationEmailBuilder {
  @Value("${frontend.base-url}")
  private String frontendBaseUrl;

  private final EmailTemplateService templateService;

  public InvitationEmailBuilder(EmailTemplateService templateService) {
    this.templateService = templateService;
  }

  public EmailRequest build(UserInvitation invitation, String token) {
    String invitationUrl = frontendBaseUrl + "/accept-invitation?token=" + token;

    Map<String, Object> vars = new HashMap<>();
    vars.put("firstName", invitation.getFirstName());
    vars.put("lastName", invitation.getLastName());
    vars.put("email", invitation.getEmail());
    vars.put("invitationUrl", invitationUrl);
    vars.put("expiresAt", invitation.getExpiresAt());

    vars.put("roles",
        invitation.getRoles()
            .stream()
            .map(role -> role.getName())
            .toList());

    vars.put("departments",
        invitation.getDepartments()
            .stream()
            .map(department -> department.getName())
            .toList());

    String html = templateService.render(
        "email/userInvitation",
        vars
    );

    EmailRequest request = new EmailRequest();
    request.setTo(invitation.getEmail());
    request.setSubject("You're invited to join eStore");
    request.setHtml(html);

    return request;
  }
}