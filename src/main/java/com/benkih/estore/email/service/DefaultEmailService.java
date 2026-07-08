package com.benkih.estore.email.service;

import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.provider.EmailProvider;
import org.springframework.stereotype.Service;

@Service
public class DefaultEmailService implements EmailService{
  private final EmailProvider provider;

  public DefaultEmailService(EmailProvider provider){
    this.provider=provider;
  }

  @Override
  public void send(EmailRequest request){
    provider.send(request);
  }

}
