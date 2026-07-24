package com.benkih.estore.inventory.controller;

import com.benkih.estore.common.dto.PaginatedResponse;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.inventory.dto.response.InventoryResponseDto;
import com.benkih.estore.inventory.entity.Inventory;
import com.benkih.estore.inventory.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/inventories")
@RequiredArgsConstructor
public class InventoryController {
  private final IInventoryService inventoryService;

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/all")
  public ResponseEntity<ApiResponse> getAllInventories(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit) {
    Pageable pageable = PageRequest.of(page - 1, limit);
    Page<InventoryResponseDto> inventoryPage = inventoryService.getAllInventories(pageable);
    PaginatedResponse<InventoryResponseDto> response = PaginatedResponse.from(inventoryPage, page);

    return ResponseEntity.ok(new ApiResponse("success", "Inventories retrieved", response));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/inventory/{slug}")
  public ResponseEntity<ApiResponse> getInventory(@PathVariable String slug){
    Inventory inventory = inventoryService.getInventoryBySlug(slug);
    InventoryResponseDto inventoryDto =
        inventoryService.convertToDto(inventory);

    return ResponseEntity.ok(
        new ApiResponse("success", "inventory returned", inventoryDto)
    );
  }


}
