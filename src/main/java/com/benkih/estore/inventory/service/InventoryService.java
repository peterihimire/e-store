package com.benkih.estore.inventory.service;

import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.inventory.dto.response.InventoryResponseDto;
import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.inventory.repository.InventoryRepository;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.entity.ProductVariant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


  @Transactional(readOnly = true)
  @Override
  public Page<InventoryResponseDto> getAllInventories(Pageable pageable, Long businessId) {
    Page<Inventory> inventoryPage = inventoryRepository.findAllByBusinessId(pageable, businessId);

    if(inventoryPage.isEmpty()){
      return Page.empty(pageable);
    }

    return inventoryPage.map(this::convertToDto);
  }


  @Override
  public Inventory getInventoryBySlug(String slug) {
    Inventory inventory =
        inventoryRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
    return inventory;
  }


  @Override
  public Map<String, Inventory> getInventoriesByVariantSlugs(Collection<String> variantSlugs) {
    return inventoryRepository.findByVariantSlugIn(variantSlugs)
        .stream()
        .collect(Collectors.toMap(inventory -> inventory.getVariant().getSlug(), Function.identity()));
  }


  //work
  @Transactional
  @Override
  public void reserve(
      String variantSlug,
      int quantity
  ) {

    if (quantity <= 0) {
      throw new BadRequestException("Quantity must be greater than zero");
    }

    Inventory inventory =
        inventoryRepository.findByProductSlugForUpdate(variantSlug)//find by
            // product variant slug
            .orElseThrow(() ->
                new ResourceNotFoundException("Inventory not found")
            );
    inventory.reserve(quantity);
    inventoryRepository.save(inventory);

  }


  @Transactional
  @Override
  public void release(String variantSlug, int quantity) {
    if (quantity <= 0) {
      throw new BadRequestException("Quantity must be greater than zero");
    }
    Inventory inventory =  inventoryRepository.findByVariantSlug(variantSlug)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
    inventory.release(quantity);
     inventoryRepository.save(inventory);
  }


  @Transactional
  @Override
  public void fulfillReservation(String variantSlug, int quantity) {
    if (quantity <= 0) {
      throw new BadRequestException("Quantity must be greater than zero");
    }
    Inventory inventory =  inventoryRepository.findByVariantSlug(variantSlug)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
    inventory.fulfillReservation(quantity);
   inventoryRepository.save(inventory);
  }


  @Transactional
  @Override
  public Inventory addStock(String slug, int quantity) {
    Inventory inventory =  inventoryRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
    inventory.addStock(quantity);
   return  inventoryRepository.save(inventory);
  }


  @Transactional
  @Override
  public Inventory markDamage(String slug, int quantity) {
    Inventory inventory =  inventoryRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
    inventory.markDamage(quantity);
   return inventoryRepository.save(inventory);
  }


  @Override
  public Inventory createInventory(ProductVariant variant) {

    if (inventoryRepository.existsByVariantSlug(variant.getSlug())) {
      throw new AlreadyExistsException("Inventory already exists for variant ");
    }

    Inventory inventory = new Inventory();

    inventory.setVariant(variant);
    inventory.setBusiness(variant.getBusiness());

    variant.setInventory(inventory);

   return  inventoryRepository.save(inventory);
  }


  @Transactional(readOnly = true)
  @Override
  public InventoryResponseDto convertToDto(Inventory inventory) {
    return new InventoryResponseDto(
        inventory.getSlug(),
        inventory.getTotalStock(),
        inventory.getAvailableStock(),
        inventory.getReservedStock(),
        inventory.getDamagedStock(),
        inventory.getAvailableStock() > 0,
        inventory.needsReorder(),
        inventory.getReorderLevel(),
        inventory.getReorderQuantity(),
        inventory.getVariant().getSku(),
        inventory.getVariant().getProduct().getName()
    );
  }
}
