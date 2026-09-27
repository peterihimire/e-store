package com.benkih.estore.payout.provider;

import com.benkih.estore.payout.dto.response.PayoutVerificationResult;
import com.benkih.estore.payout.enums.PayoutProvider;
import com.benkih.estore.payout.webhook.PayoutWebhookEvent;

public interface PayoutWebhookHandler {
  PayoutProvider supports();
  void verifySignature(String signature, String payload);
  PayoutWebhookEvent parseWebhook(String payload);
  PayoutVerificationResult verify(String providerReference);
}
