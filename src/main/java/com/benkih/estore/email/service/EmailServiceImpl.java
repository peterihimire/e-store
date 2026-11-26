package com.benkih.estore.email.service;

import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.provider.EmailProvider;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private final EmailProvider provider;

    public EmailServiceImpl(EmailProvider provider) {
        this.provider = provider;
    }

    @Override
    public String send(EmailRequest request) {
        return provider.send(request);
    }
}
