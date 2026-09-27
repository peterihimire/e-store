package com.benkih.estore.payout.repository;

import com.benkih.estore.payout.entity.PayoutRecipient;
import com.benkih.estore.payout.enums.PayoutProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayoutRecipientRepository extends JpaRepository<PayoutRecipient, Long> {

  Optional<PayoutRecipient> findByBankAccountIdAndProvider(
      Long bankAccountId,
      PayoutProvider provider
  );

  Optional<PayoutRecipient> findByBankAccountIdAndProviderAndActiveTrue(
      Long bankAccountId,
      PayoutProvider provider
  );

  boolean existsByBankAccountIdAndProvider(
      Long bankAccountId,
      PayoutProvider provider
  );
}
