package com.benkih.estore.inventory.repository;

import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

  @EntityGraph(attributePaths = {"product"})
  List<Inventory> findAll();

  @EntityGraph(attributePaths = {"product"})
  Optional<Inventory> findBySlug(String slug);

  @EntityGraph(attributePaths = {"product"})
  Optional<Inventory> findByBusinessSlugAndSlug(
      String businessSlug,
      String slug
  );

  Page<Inventory> findAllByBusinessId(Pageable pageable,  Long businessid);

  Optional<Inventory> findByProductSlug(String productSlug);

  List<Inventory> findByProductSlugIn(Collection<String> productSlugs);

  boolean existsByProductSlug(String productSlug);
}
