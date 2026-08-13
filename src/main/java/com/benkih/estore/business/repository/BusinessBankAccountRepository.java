package com.benkih.estore.business.repository;

import com.benkih.estore.business.entity.BusinessBankAccount;
import com.benkih.estore.business.entity.BusinessMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessBankAccountRepository extends JpaRepository<BusinessBankAccount, Long> {

  Optional<BusinessBankAccount> findByBusinessIdAndId(Long businessId, Long id);

  List<BusinessBankAccount> findByBusinessId(Long businessId);

  Optional<BusinessBankAccount> findByBusinessIdAndDefaultAccountTrue(Long businessId);
}
