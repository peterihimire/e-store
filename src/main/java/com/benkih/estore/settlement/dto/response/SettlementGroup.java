package com.benkih.estore.settlement.dto.response;

import com.benkih.estore.common.enums.CurrencyCode;

public record SettlementGroup(
    Long businessId,
    CurrencyCode currency
) {}
