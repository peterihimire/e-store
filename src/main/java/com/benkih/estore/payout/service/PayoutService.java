package com.benkih.estore.payout.service;

import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.payout.dto.response.PayoutResponseDto;
import com.benkih.estore.payout.entity.Payout;
import com.benkih.estore.webhook.handler.WebhookEvent;
import java.math.BigDecimal;

public interface PayoutService {
    PayoutResponseDto requestPayout(
            Long businessId,
            String bankAccountSlug,
            CurrencyCode currency,
            BigDecimal amount,
            String idempotencyKey);

    void markPayoutSuccessful(Payout payout, String providerReference);

    void handleWebhook(WebhookEvent event, String signature, String payload);

    PayoutResponseDto convertToDto(Payout payout);
}
