package com.benkih.estore.department.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.department.dto.request.AssignUsersToDepartmentRequest;
import com.benkih.estore.department.dto.request.CreateDepartmentRequest;
import com.benkih.estore.department.dto.request.UpdateDepartmentRequest;
import com.benkih.estore.department.entity.Department;
import com.benkih.estore.department.service.DepartmentService;
import com.benkih.estore.security.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/departments")
@RequiredArgsConstructor
public class DepartmentController {
  private final DepartmentService departmentService;
  private final TenantContext tenantContext;

  @PostMapping("/add")
  @PreAuthorize("hasAuthority('DEPARTMENT_CREATE')")
  public ResponseEntity<ApiResponse> createDepartment(
      @Valid @RequestBody CreateDepartmentRequest request) {
    Long businessId = tenantContext.getBusinessId();

    Department department = departmentService.createDepartment(request, businessId);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiResponse(
            "success",
            "Department created successfully",
            departmentService.convertToDto(department)
        ));
  }

  @GetMapping("/all")
  @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
  public ResponseEntity<ApiResponse> getDepartments() {

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Departments retrieved successfully",
            departmentService.getDepartments()
        )
    );
  }

  @GetMapping("/department/{slug}")
  @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
  public ResponseEntity<ApiResponse> getDepartment(
      @PathVariable String slug) {

    Department department = departmentService.getDepartment(slug);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Department retrieved successfully",
            departmentService.convertToDto(department)
        )
    );
  }

  @PutMapping("/department/{slug}")
  @PreAuthorize("hasAuthority('DEPARTMENT_UPDATE')")
  public ResponseEntity<ApiResponse> updateDepartment(
      @PathVariable String slug,
      @Valid @RequestBody UpdateDepartmentRequest request) {

    Department department = departmentService.updateDepartment(slug, request);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Department updated successfully",
            departmentService.convertToDto(department)
        )
    );
  }

  @DeleteMapping("/department/{slug}")
  @PreAuthorize("hasAuthority('DEPARTMENT_DELETE')")
  public ResponseEntity<ApiResponse> deleteDepartment(@PathVariable String slug) {

    departmentService.deleteDepartment(slug);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Department deleted successfully",
            null
        )
    );
  }

  @PreAuthorize("hasAuthority('DEPARTMENT_UPDATE')")
  @PostMapping("/department/{slug}/users")
  public ResponseEntity<ApiResponse> assignUsers(
      @PathVariable String slug,
      @RequestBody @Valid AssignUsersToDepartmentRequest request) {

    departmentService.assignUsers(slug, request);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Users assigned successfully",
            null));
  }

  @PreAuthorize("hasAuthority('DEPARTMENT_UPDATE')")
  @DeleteMapping("/department/{slug}/users/{userSlug}")
  public ResponseEntity<ApiResponse> removeUser(
      @PathVariable String slug,
      @PathVariable String userSlug) {

    departmentService.removeUser(slug, userSlug);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "User removed successfully",
            null));
  }

  @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
  @GetMapping("/department/{slug}/users")
  public ResponseEntity<ApiResponse> getDepartmentUsers(@PathVariable String slug) {

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Department users fetched",
            departmentService.getDepartmentUsers(slug)));
  }
}
