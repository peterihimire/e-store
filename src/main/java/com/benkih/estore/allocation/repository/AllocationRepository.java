package com.benkih.estore.allocation.repository;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
  boolean existsByPaymentId(Long paymentId);

  List<Allocation> findByPaymentId(Long paymentId);

}
