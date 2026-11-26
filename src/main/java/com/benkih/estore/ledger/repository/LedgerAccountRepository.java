package com.benkih.estore.ledger.repository;

import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.ledger.entity.LedgerAccount;
import com.benkih.estore.ledger.enums.LedgerAccountType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, Long> {

    Optional<LedgerAccount> findByBusinessIdAndAccountTypeAndCurrency(
            Long businessId, LedgerAccountType accountType, CurrencyCode currency);

    Optional<LedgerAccount> findByBusinessIsNullAndAccountTypeAndCurrency(
            LedgerAccountType accountType, CurrencyCode currency);
}
