package com.benkih.estore.department.service;

import com.benkih.estore.department.dto.request.AssignUsersToDepartmentRequest;
import com.benkih.estore.department.dto.request.CreateDepartmentRequest;
import com.benkih.estore.department.dto.request.UpdateDepartmentRequest;
import com.benkih.estore.department.dto.response.DepartmentResponseDto;
import com.benkih.estore.department.entity.Department;
import com.benkih.estore.user.dto.response.UserResponseDto;

import java.util.List;

public interface IDepartmentService {

  Department createDepartment(CreateDepartmentRequest request, Long businessId);

  Department updateDepartment(String slug, UpdateDepartmentRequest request);

  void deleteDepartment(String slug);

  Department getDepartment(String slug);

  List<DepartmentResponseDto> getDepartments();

  DepartmentResponseDto convertToDto(Department department);

  void assignUsers(String departmentSlug, AssignUsersToDepartmentRequest request);

  void removeUser(String departmentSlug, String userSlug);

  List<UserResponseDto> getDepartmentUsers(String departmentSlug);
}