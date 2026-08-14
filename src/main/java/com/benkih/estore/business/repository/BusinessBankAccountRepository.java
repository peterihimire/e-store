package com.benkih.estore.business.repository;

import com.benkih.estore.business.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessBankAccountRepository extends JpaRepository<BankAccount, Long> {

  Optional<BankAccount> findByBusinessSlugAndSlug(String businessSlug,
                                                String slug);

  List<BankAccount> findByBusinessSlug(String businessSlug);

  Optional<BankAccount> findByBusinessSlugAndDefaultAccountTrue(String businessSlug);
}
