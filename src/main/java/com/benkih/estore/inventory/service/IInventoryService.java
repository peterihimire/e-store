package com.benkih.estore.inventory.service;

import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.product.entity.Product;

import java.util.Collection;
import java.util.Map;

public interface IInventoryService {
  Inventory getByProductSlug(String productSlug);

  Map<String, Inventory> getInventoriesByProductSlugs(Collection<String> productSlugs);

  void reserve(String productSlug, int quantity);

  void release(String productSlug, int quantity);

  void fufillReservation(String productSlug, int quantity);

  void addStock(String productSlug, int quantity);

  void markDamage(String productSlug, int quantity);

  Inventory createInventory(Product product);
}
