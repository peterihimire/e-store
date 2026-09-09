package com.benkih.estore.business.service;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.business.entity.BusinessBalance;
import com.benkih.estore.common.enums.CurrencyCode;

public interface IBusinessBalanceService {
  BusinessBalance getOrCreate(
      Business business,
      CurrencyCode currency
  );
}
