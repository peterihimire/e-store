package com.benkih.estore.department.service;

import com.benkih.estore.department.dto.request.AssignUsersToDepartmentRequest;
import com.benkih.estore.department.dto.request.CreateDepartmentRequest;
import com.benkih.estore.department.dto.request.UpdateDepartmentRequest;
import com.benkih.estore.department.dto.response.DepartmentResponseDto;
import com.benkih.estore.department.entity.Department;
import com.benkih.estore.user.dto.response.UserResponseDto;
import java.util.List;

public interface DepartmentService {
    Department createDepartment(CreateDepartmentRequest request, Long businessId);

    Department updateDepartment(String slug, UpdateDepartmentRequest request);

    void deleteDepartment(String slug);

    Department getDepartment(String slug);

    List<DepartmentResponseDto> getDepartments(Long businessId);

    List<DepartmentResponseDto> getDepartmentsAdmin();

    void assignUsers(String departmentSlug, AssignUsersToDepartmentRequest request);

    void removeUser(String departmentSlug, String userSlug);

    List<UserResponseDto> getDepartmentUsers(String departmentSlug);

    DepartmentResponseDto convertToDto(Department department);
}
