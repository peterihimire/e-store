package com.benkih.estore.inventory.repository;

import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
    SELECT i
    FROM Inventory i
    JOIN i.product p
    WHERE p.slug = :productSlug
""")
  Optional<Inventory> findByProductSlugForUpdate(
      @Param("productSlug") String productSlug
  );


  Optional<Inventory> findByVariantSlug(String variantSlug);

  List<Inventory> findByVariantSlugIn(Collection<String> variantSlugs);

  boolean existsByVariantSlug(String variantSlug);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
    SELECT i
    FROM Inventory i
    JOIN i.variant pv
    WHERE pv.slug = :variantSlug
""")
  Optional<Inventory> findByVariantSlugForUpdate(
      @Param("variantSlug") String variantSlug
  );
}
