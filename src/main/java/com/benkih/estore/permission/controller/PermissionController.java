package com.benkih.estore.permission.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.permission.dto.response.PermissionResponseDto;
import com.benkih.estore.permission.entity.Permission;
import com.benkih.estore.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/permissions")
public class PermissionController {
  private final PermissionService permissionService;

  @GetMapping
  @PreAuthorize("hasAuthority('ROLE_READ')")
  public ResponseEntity<ApiResponse> getPermissions() {
    List<Permission> permissions =  permissionService.getPermissions();
    List<PermissionResponseDto> responseDtos =
        permissionService.getConvertedPermissions(permissions);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Permissions retrieved successfully",
            responseDtos
        )
    );
  }

  @GetMapping("/{slug}")
  @PreAuthorize("hasAuthority('ROLE_READ')")
  public ResponseEntity<ApiResponse> getPermission(@PathVariable String slug) {
    PermissionResponseDto response =
        permissionService.getPermissionBySlug(slug);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Permission retrieved successfully",
            response
        )
    );
  }
}
