package com.benkih.estore.inventory.service;

import com.benkih.estore.inventory.dto.response.InventoryResponseDto;
import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

public interface IInventoryService {
  Inventory getByProductSlug(String productSlug);

  Page<InventoryResponseDto> getAllInventories(Pageable pageable, Long businessId);

  Inventory getInventoryBySlug(String slug);

  Map<String, Inventory> getInventoriesByVariantSlugs(Collection<String> productSlugs);

  void reserve(String variantSlug, int quantity);

  void release(String variantSlug, int quantity);

  void fulfillReservation(String variantSlug, int quantity);

  Inventory addStock(String slug, int quantity);

  Inventory markDamage(String slug, int quantity);

  Inventory createInventory(ProductVariant variant);

  @Transactional(readOnly = true)
  InventoryResponseDto convertToDto(Inventory inventory);
}
