package com.benkih.estore.email.provider;

import com.benkih.estore.email.dto.EmailRequest;

public interface EmailProvider {
  void send(EmailRequest request);
}
