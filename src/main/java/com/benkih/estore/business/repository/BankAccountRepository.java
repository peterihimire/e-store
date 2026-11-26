package com.benkih.estore.business.repository;

import com.benkih.estore.business.entity.BankAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findBySlugAndBusinessId(String slug, Long businessId);

    boolean existsByBusinessIdAndAccountNumber(Long businessId, String accountNumber);
}
