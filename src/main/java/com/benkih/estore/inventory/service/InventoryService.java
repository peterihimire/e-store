package com.benkih.estore.inventory.service;

import com.benkih.estore.inventory.dto.response.InventoryResponseDto;
import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.product.entity.ProductVariant;
import java.util.Collection;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {
    Inventory getByProductSlug(String variantSlug);

    Page<InventoryResponseDto> getAllInventories(Pageable pageable, Long businessId);

    InventoryResponseDto getInventoryBySlug(String slug);

    Map<String, Inventory> getInventoriesByVariantSlugs(Collection<String> variantSlugs);

    void reserve(String variantSlug, int quantity);

    void release(String variantSlug, int quantity);

    void fulfillReservation(String variantSlug, int quantity);

    InventoryResponseDto addStock(String slug, int quantity);

    InventoryResponseDto markDamage(String slug, int quantity);

    Inventory createInventory(ProductVariant variant);

    InventoryResponseDto convertToDto(Inventory inventory);
}
