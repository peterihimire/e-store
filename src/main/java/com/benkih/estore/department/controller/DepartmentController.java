package com.benkih.estore.department.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.department.dto.request.AssignUsersToDepartmentRequest;
import com.benkih.estore.department.dto.request.CreateDepartmentRequest;
import com.benkih.estore.department.dto.request.UpdateDepartmentRequest;
import com.benkih.estore.department.dto.response.DepartmentResponseDto;
import com.benkih.estore.department.entity.Department;
import com.benkih.estore.department.service.DepartmentService;
import com.benkih.estore.security.tenant.TenantContext;
import com.benkih.estore.user.dto.response.UserResponseDto;
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

@Tag(name = "Departments", description = "Business departments and their members")
@RestController
@RequestMapping("${api.prefix}/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;
    private final TenantContext tenantContext;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('DEPARTMENT_CREATE')")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request) {
        Long businessId = tenantContext.getBusinessId();

        Department department = departmentService.createDepartment(request, businessId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                "success",
                                "Department created successfully",
                                departmentService.convertToDto(department)));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    public ResponseEntity<ApiResponse<List<DepartmentResponseDto>>> getDepartments() {
        Long businessId = tenantContext.getBusinessId();

        List<DepartmentResponseDto> departmentsDto = departmentService.getDepartments(businessId);

        return ResponseEntity.ok(
                new ApiResponse<>("success", "Departments retrieved successfully", departmentsDto));
    }

    @GetMapping("/department/{slug}")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> getDepartment(
            @PathVariable String slug) {

        Department department = departmentService.getDepartment(slug);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "Department retrieved successfully",
                        departmentService.convertToDto(department)));
    }

    @PutMapping("/department/{slug}")
    @PreAuthorize("hasAuthority('DEPARTMENT_UPDATE')")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> updateDepartment(
            @PathVariable String slug, @Valid @RequestBody UpdateDepartmentRequest request) {

        Department department = departmentService.updateDepartment(slug, request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "Department updated successfully",
                        departmentService.convertToDto(department)));
    }

    @DeleteMapping("/department/{slug}")
    @PreAuthorize("hasAuthority('DEPARTMENT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable String slug) {

        departmentService.deleteDepartment(slug);

        return ResponseEntity.ok(
                new ApiResponse<>("success", "Department deleted successfully", null));
    }

    @PreAuthorize("hasAuthority('DEPARTMENT_UPDATE')")
    @PostMapping("/department/{slug}/users")
    public ResponseEntity<ApiResponse<Void>> assignUsers(
            @PathVariable String slug, @RequestBody @Valid AssignUsersToDepartmentRequest request) {

        departmentService.assignUsers(slug, request);

        return ResponseEntity.ok(new ApiResponse<>("success", "Users assigned successfully", null));
    }

    @PreAuthorize("hasAuthority('DEPARTMENT_UPDATE')")
    @DeleteMapping("/department/{slug}/users/{userSlug}")
    public ResponseEntity<ApiResponse<Void>> removeUser(
            @PathVariable String slug, @PathVariable String userSlug) {

        departmentService.removeUser(slug, userSlug);

        return ResponseEntity.ok(new ApiResponse<>("success", "User removed successfully", null));
    }

    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    @GetMapping("/department/{slug}/users")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getDepartmentUsers(
            @PathVariable String slug) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "Department users fetched",
                        departmentService.getDepartmentUsers(slug)));
    }
}
