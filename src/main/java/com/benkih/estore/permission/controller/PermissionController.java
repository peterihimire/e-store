package com.benkih.estore.permission.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.permission.dto.response.PermissionResponseDto;
import com.benkih.estore.permission.entity.Permission;
import com.benkih.estore.permission.service.PermissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Permissions", description = "Available permission authorities")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/permissions")
public class PermissionController {
    private final PermissionService permissionService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<List<PermissionResponseDto>>> getPermissions() {
        List<Permission> permissions = permissionService.getPermissions();
        List<PermissionResponseDto> responseDtos =
                permissionService.getConvertedPermissions(permissions);

        return ResponseEntity.ok(
                new ApiResponse<>("success", "Permissions retrieved successfully", responseDtos));
    }

    @GetMapping("/{slug}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<PermissionResponseDto>> getPermission(
            @PathVariable String slug) {
        PermissionResponseDto response = permissionService.getPermissionBySlug(slug);

        return ResponseEntity.ok(
                new ApiResponse<>("success", "Permission retrieved successfully", response));
    }
}
