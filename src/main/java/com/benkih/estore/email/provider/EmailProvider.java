package com.benkih.estore.email.provider;

import com.benkih.estore.email.dto.EmailRequest;

public interface EmailProvider {

    /**
     * Sends a message and returns the provider's message id, or {@code null} when the provider does
     * not expose one.
     */
    String send(EmailRequest request);
}
