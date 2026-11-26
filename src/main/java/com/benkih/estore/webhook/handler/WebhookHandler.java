package com.benkih.estore.webhook.handler;

import com.benkih.estore.common.enums.PaymentProvider;

public interface WebhookHandler {
    PaymentProvider supports();

    void verifySignature(String signature, String payload);

    WebhookEvent parseWebhook(String payload);

    //  PaymentWebhookEvent parseWebhook(
    //      String payload
    //  );
}
