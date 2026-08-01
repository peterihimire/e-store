package com.benkih.estore.department.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.department.dto.request.CreateDepartmentRequest;
import com.benkih.estore.department.dto.request.UpdateDepartmentRequest;
import com.benkih.estore.department.entity.Department;
import com.benkih.estore.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {
  private final DepartmentService departmentService;

  @PostMapping
  @PreAuthorize("hasAuthority('DEPARTMENT_CREATE')")
  public ResponseEntity<ApiResponse> createDepartment(
      @Valid @RequestBody CreateDepartmentRequest request) {

    Department department = departmentService.createDepartment(request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiResponse(
            "success",
            "Department created successfully",
            departmentService.convertToDto(department)
        ));
  }

  @GetMapping
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

  @GetMapping("/{slug}")
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

  @PutMapping("/{slug}")
  @PreAuthorize("hasAuthority('DEPARTMENT_UPDATE')")
  public ResponseEntity<ApiResponse> updateDepartment(
      @PathVariable String slug,
      @Valid @RequestBody UpdateDepartmentRequest request) {

    Department department =
        departmentService.updateDepartment(slug, request);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Department updated successfully",
            departmentService.convertToDto(department)
        )
    );
  }

  @DeleteMapping("/{slug}")
  @PreAuthorize("hasAuthority('DEPARTMENT_DELETE')")
  public ResponseEntity<ApiResponse> deleteDepartment(
      @PathVariable String slug) {

    departmentService.deleteDepartment(slug);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Department deleted successfully",
            null
        )
    );
  }
}
