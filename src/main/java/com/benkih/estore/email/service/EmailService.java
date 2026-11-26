package com.benkih.estore.email.service;

import com.benkih.estore.email.dto.EmailRequest;

public interface EmailService {
    String send(EmailRequest request);
}
