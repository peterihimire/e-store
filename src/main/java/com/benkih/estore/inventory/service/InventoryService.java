package com.benkih.estore.inventory.service;

import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.inventory.repository.InventoryRepository;
import com.benkih.estore.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService implements IInventoryService{
  private final InventoryRepository inventoryRepository;

  @Override
  public Inventory getByProductSlug(String productSlug) {
    return inventoryRepository.findByProductSlug(productSlug)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product."));
  }

  @Override
  public Map<String, Inventory> getInventoriesByProductSlugs(Collection<String> productSlugs) {
    return inventoryRepository.findByProductSlugIn(productSlugs)
        .stream()
        .collect(Collectors.toMap(inventory -> inventory.getProduct().getSlug(), Function.identity()));
  }

  @Override
  public void reserve(String productSlug, int quantity) {
    Inventory inventory = getByProductSlug(productSlug);
    inventory.reserve(quantity);
    inventoryRepository.save(inventory);
  }

  @Override
  public void release(String productSlug, int quantity) {
    Inventory inventory = getByProductSlug(productSlug);
    inventory.release(quantity);
    inventoryRepository.save(inventory);
  }

  @Override
  public void fufillReservation(String productSlug, int quantity) {
    Inventory inventory = getByProductSlug(productSlug);
    inventory.fulfillReservation(quantity);
    inventoryRepository.save(inventory);
  }

  @Override
  public void addStock(String productSlug, int quantity) {
    Inventory inventory = getByProductSlug(productSlug);
    inventory.addStock(quantity);
    inventoryRepository.save(inventory);
  }

  @Override
  public void markDamage(String productSlug, int quantity) {
    Inventory inventory = getByProductSlug(productSlug);
    inventory.markDamage(quantity);
    inventoryRepository.save(inventory);
  }

  @Override
  public Inventory createInventory(Product product) {

    if (inventoryRepository.existsByProductSlug(product.getSlug())) {
      log.warn("Inventory already exists for product {}", product.getSlug());
      throw new AlreadyExistsException("Inventory already exists for product ");
    }

    Inventory inventory = new Inventory();
    inventory.setProduct(product);

   return  inventoryRepository.save(inventory);
//    log.info("Inventory created for product {}", product.getSlug());
  }
}
