package com.benkih.estore.email.provider;

import com.benkih.estore.email.dto.EmailRequest;

public abstract class AbstractEmailProvider implements EmailProvider {

  protected void validate(EmailRequest request){
    if(request.getTo()==null){
      throw new IllegalArgumentException("Recipient is required");
    }
  }
}