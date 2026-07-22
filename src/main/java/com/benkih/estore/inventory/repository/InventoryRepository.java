package com.benkih.estore.inventory.repository;

import com.benkih.estore.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

  Optional<Inventory> findBySlug(String slug);

  Optional<Inventory> findByProductSlug(String productSlug);

  List<Inventory> findByProductSlugIn(Collection<String> productSlugs);

  boolean existsByProductSlug(String productSlug);
}
