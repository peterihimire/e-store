package com.benkih.estore.inventory.controller;

import com.benkih.estore.common.dto.PaginatedResponse;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.inventory.dto.request.InventoryAdjustmentRequest;
import com.benkih.estore.inventory.dto.response.InventoryResponseDto;
import com.benkih.estore.inventory.service.InventoryService;
import com.benkih.estore.security.tenant.TenantContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Inventory", description = "Stock levels, additions and damage records")
@RestController
@RequestMapping("${api.prefix}/inventories")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;
    private final TenantContext tenantContext;

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PaginatedResponse<InventoryResponseDto>>> getAllInventories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Long businessId = tenantContext.getBusinessId();

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<InventoryResponseDto> inventoryPage =
                inventoryService.getAllInventories(pageable, businessId);
        PaginatedResponse<InventoryResponseDto> response =
                PaginatedResponse.from(inventoryPage, page);

        return ResponseEntity.ok(new ApiResponse<>("success", "Inventories retrieved", response));
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping("/inventory/{slug}")
    public ResponseEntity<ApiResponse<InventoryResponseDto>> getInventory(
            @PathVariable String slug) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "inventory returned",
                        inventoryService.getInventoryBySlug(slug)));
    }

    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    @PostMapping("/inventory/{slug}/add")
    public ResponseEntity<ApiResponse<InventoryResponseDto>> addStock(
            @PathVariable String slug, @RequestBody InventoryAdjustmentRequest qty) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "add inventory success",
                        inventoryService.addStock(slug, qty.getQuantity())));
    }

    @PreAuthorize("hasAuthority('INVENTORY_DAMAGE')")
    @PostMapping("/inventory/{slug}/damage")
    public ResponseEntity<ApiResponse<InventoryResponseDto>> markDamage(
            @PathVariable String slug, @RequestBody InventoryAdjustmentRequest qty) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "mark damage success",
                        inventoryService.markDamage(slug, qty.getQuantity())));
    }
}
