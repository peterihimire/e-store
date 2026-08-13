package com.benkih.estore.business.repository;

import com.benkih.estore.business.entity.BusinessBankAccount;
import com.benkih.estore.business.entity.BusinessCustomer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessCustomerRepository  extends JpaRepository<BusinessCustomer, Long> {
  Optional<BusinessCustomer> findByBusinessIdAndId(Long businessId, Long id);

  Optional<BusinessCustomer> findByBusinessIdAndEmail(Long businessId, String email);

  Page<BusinessCustomer> findByBusinessId(Long businessId, Pageable pageable);
}
