package com.benkih.estore.role.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.role.dto.request.CreateRoleRequest;
import com.benkih.estore.role.dto.request.UpdateRoleRequest;
import com.benkih.estore.role.dto.response.RoleResponseDto;
import com.benkih.estore.role.entity.Role;
import com.benkih.estore.role.service.RoleService;
import com.benkih.estore.security.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/roles")
public class RoleController {
  private final RoleService roleService;
  private final TenantContext tenantContext;

  @PostMapping("/add")
  @PreAuthorize("hasAuthority('ROLE_CREATE')")
  public ResponseEntity<ApiResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
    Long businessId = tenantContext.getBusinessId();

    Role role = roleService.createRole(request, businessId);
    RoleResponseDto responseDto = roleService.convertToDto(role);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiResponse(
            "success",
            "Role created successfully",
            responseDto
        ));
  }

  @GetMapping("/all")
  @PreAuthorize("hasAuthority('ROLE_READ')")
  public ResponseEntity<ApiResponse> getRoles() {
    List<RoleResponseDto> rolesDto = roleService.getRoles();

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Roles retrieved successfully",
            rolesDto
        )
    );
  }

  @GetMapping("/{slug}")
  @PreAuthorize("hasAuthority('ROLE_READ')")
  public ResponseEntity<ApiResponse> getRole(@PathVariable String slug) {
    Role role = roleService.getRole(slug);
    RoleResponseDto responseDto =  roleService.convertToDto(role);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Role retrieved successfully",
           responseDto
        )
    );
  }

  @PutMapping("/role/{slug}")
  @PreAuthorize("hasAuthority('ROLE_UPDATE')")
  public ResponseEntity<ApiResponse> updateRole(
      @PathVariable String slug,
      @Valid @RequestBody UpdateRoleRequest request) {

    Role role = roleService.updateRole(slug, request);
    RoleResponseDto responseDto = roleService.convertToDto(role);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Role updated successfully",
            responseDto
        )
    );
  }

  @DeleteMapping("/{slug}")
  @PreAuthorize("hasAuthority('ROLE_DELETE')")
  public ResponseEntity<ApiResponse> deleteRole(
      @PathVariable String slug) {

    roleService.deleteRole(slug);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Role deleted successfully",
            null
        )
    );
  }
}
