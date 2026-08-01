package com.benkih.estore.department.service;

import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.department.dto.request.CreateDepartmentRequest;
import com.benkih.estore.department.dto.request.UpdateDepartmentRequest;
import com.benkih.estore.department.dto.response.DepartmentResponseDto;
import com.benkih.estore.department.entity.Department;
import com.benkih.estore.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DepartmentService implements IDepartmentService{
  private final DepartmentRepository departmentRepository;

  @Override
  public Department createDepartment(CreateDepartmentRequest request) {

    if (departmentRepository.existsByNameIgnoreCase(request.getName())) {
      throw new AlreadyExistsException("Department already exists");}

    Department department = new Department();

    department.setName(request.getName().trim());
    department.setDescription(request.getDescription());

    return departmentRepository.save(department);
  }

  @Override
  public Department updateDepartment(String slug, UpdateDepartmentRequest request) {

    Department department = getDepartment(slug);

    departmentRepository.findByNameIgnoreCase(request.getName())
        .ifPresent(existing -> {

          if (!existing.getSlug().equals(slug)) {
            throw new AlreadyExistsException("Department already exists");}
        });

    department.setName(request.getName().trim());
    department.setDescription(request.getDescription());

    return departmentRepository.save(department);
  }

  @Override
  public void deleteDepartment(String slug) {

    Department department = getDepartment(slug);

    departmentRepository.delete(department);
  }

  @Override
  @Transactional(readOnly = true)
  public Department getDepartment(String slug) {

    return departmentRepository.findBySlug(slug)
        .orElseThrow(() ->
            new ResourceNotFoundException("Department not found"));
  }

  @Override
  @Transactional(readOnly = true)
  public List<DepartmentResponseDto> getDepartments() {

    return departmentRepository.findAll()
        .stream()
        .map(this::convertToDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public DepartmentResponseDto convertToDto(Department department) {

    return new DepartmentResponseDto(
        department.getSlug(),
        department.getName(),
        department.getDescription()
    );
  }
}
