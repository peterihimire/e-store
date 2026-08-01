package com.benkih.estore.department.service;

import com.benkih.estore.department.dto.request.CreateDepartmentRequest;
import com.benkih.estore.department.dto.request.UpdateDepartmentRequest;
import com.benkih.estore.department.dto.response.DepartmentResponseDto;
import com.benkih.estore.department.entity.Department;

import java.util.List;

public interface IDepartmentService {

  Department createDepartment(CreateDepartmentRequest request);

  Department updateDepartment(String slug, UpdateDepartmentRequest request);

  void deleteDepartment(String slug);

  Department getDepartment(String slug);

  List<DepartmentResponseDto> getDepartments();

  DepartmentResponseDto convertToDto(Department department);
}