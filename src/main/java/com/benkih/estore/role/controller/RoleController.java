package com.benkih.estore.role.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.role.dto.request.CreateRoleRequest;
import com.benkih.estore.role.dto.request.UpdateRoleRequest;
import com.benkih.estore.role.dto.response.RoleResponseDto;
import com.benkih.estore.role.entity.Role;
import com.benkih.estore.role.service.RoleService;
import com.benkih.estore.security.tenant.TenantContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Roles", description = "Roles and their permission assignments")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/roles")
public class RoleController {
    private final RoleService roleService;
    private final TenantContext tenantContext;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<ApiResponse<RoleResponseDto>> createRole(
            @Valid @RequestBody CreateRoleRequest request) {
        Long businessId = tenantContext.getBusinessId();

        Role role = roleService.createRole(request, businessId);
        RoleResponseDto responseDto = roleService.convertToDto(role);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("success", "Role created successfully", responseDto));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<List<RoleResponseDto>>> getRoles() {
        Long businessId = tenantContext.getBusinessId();

        List<RoleResponseDto> rolesDto = roleService.getRoles(businessId);

        return ResponseEntity.ok(
                new ApiResponse<>("success", "Roles retrieved successfully", rolesDto));
    }

    @GetMapping("/{slug}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<RoleResponseDto>> getRole(@PathVariable String slug) {
        Role role = roleService.getRole(slug);
        RoleResponseDto responseDto = roleService.convertToDto(role);

        return ResponseEntity.ok(
                new ApiResponse<>("success", "Role retrieved successfully", responseDto));
    }

    @PutMapping("/role/{slug}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<RoleResponseDto>> updateRole(
            @PathVariable String slug, @Valid @RequestBody UpdateRoleRequest request) {

        Role role = roleService.updateRole(slug, request);
        RoleResponseDto responseDto = roleService.convertToDto(role);

        return ResponseEntity.ok(
                new ApiResponse<>("success", "Role updated successfully", responseDto));
    }

    @DeleteMapping("/{slug}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable String slug) {

        roleService.deleteRole(slug);

        return ResponseEntity.ok(new ApiResponse<>("success", "Role deleted successfully", null));
    }
}
