package com.benkih.estore.allocation.repository;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
}
