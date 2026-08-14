package com.benkih.estore.business.repository;

import com.benkih.estore.business.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessCustomerRepository  extends JpaRepository<Customer, Long> {
  Optional<Customer> findByBusinessSlugAndSlug(String businessSlug,
                                               String slug);

  Optional<Customer> findByBusinessSlugAndEmail(String businessSlug,
                                                String email);

  Page<Customer> findByBusinessSlug(String businessSlug, Pageable pageable);
}
