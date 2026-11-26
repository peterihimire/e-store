package com.benkih.estore.business.service;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.business.entity.Business;
import com.benkih.estore.business.entity.BusinessBalance;
import com.benkih.estore.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.util.List;

public interface BusinessBalanceService {
    BusinessBalance getOrCreate(Business business, CurrencyCode currency);

    void increasePending(Business business, CurrencyCode currency, BigDecimal amount);

    void decreasePending(Business business, CurrencyCode currency, BigDecimal amount);

    void decreaseAvailable(Business business, CurrencyCode currency, BigDecimal amount);

    void movePendingToAvailable(Business business, CurrencyCode currency, BigDecimal amount);

    void moveAvailableToPayout(Business business, CurrencyCode currency, BigDecimal amount);

    void movePayoutToAvailable(Business business, CurrencyCode currency, BigDecimal amount);

    void recordAllocations(List<Allocation> allocations);
}
