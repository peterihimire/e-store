package com.benkih.estore.email.service;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class EmailTemplateService {
  private final TemplateEngine templateEngine;

  public EmailTemplateService(TemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
  }

  public String render(String template, Map<String, Object> variables) {
    Context context = new Context();
    context.setVariables(variables);
    return templateEngine.process(template, context);
  }
}
