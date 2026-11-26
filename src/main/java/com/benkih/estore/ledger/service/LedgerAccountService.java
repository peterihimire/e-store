package com.benkih.estore.ledger.service;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.ledger.entity.LedgerAccount;
import com.benkih.estore.ledger.enums.LedgerAccountType;

public interface LedgerAccountService {
    LedgerAccount getOrCreateSellerAccount(
            Business business, LedgerAccountType type, CurrencyCode currency);

    LedgerAccount getOrCreatePlatformAccount(LedgerAccountType type, CurrencyCode currency);
}
