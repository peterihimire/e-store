package com.benkih.estore.inventory.service;

import com.benkih.estore.inventory.dto.response.InventoryResponseDto;
import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.product.entity.Product;
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

  Map<String, Inventory> getInventoriesByProductSlugs(Collection<String> productSlugs);

  void reserve(String productSlug, int quantity);

  void release(String productSlug, int quantity);

  void fulfillReservation(String productSlug, int quantity);

  Inventory addStock(String slug, int quantity);

  Inventory markDamage(String slug, int quantity);

  Inventory createInventory(Product product);

  @Transactional(readOnly = true)
  InventoryResponseDto convertToDto(Inventory inventory);
}
