package com.benkih.estore.business.repository;

import com.benkih.estore.business.entity.BusinessBalance;
import com.benkih.estore.common.enums.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessBalanceRepository extends JpaRepository<BusinessBalance, Long> {

  Optional<BusinessBalance> findByBusinessIdAndCurrency(
      Long businessId,
      CurrencyCode currency
  );
}
