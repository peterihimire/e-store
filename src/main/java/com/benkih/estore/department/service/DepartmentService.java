package com.benkih.estore.department.service;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.business.repository.BusinessRepository;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.department.dto.request.AssignUsersToDepartmentRequest;
import com.benkih.estore.department.dto.request.CreateDepartmentRequest;
import com.benkih.estore.department.dto.request.UpdateDepartmentRequest;
import com.benkih.estore.department.dto.response.DepartmentResponseDto;
import com.benkih.estore.department.entity.Department;
import com.benkih.estore.department.repository.DepartmentRepository;
import com.benkih.estore.user.dto.response.UserResponseDto;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.repository.UserRepository;
import com.benkih.estore.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@RequiredArgsConstructor
@Service
public class DepartmentService implements IDepartmentService{
  private final DepartmentRepository departmentRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  private final BusinessRepository businessRepository;


  @Override
  public Department createDepartment(CreateDepartmentRequest request, Long businessId) {

    if (departmentRepository.existsByNameIgnoreCaseAndBusinessId(request.getName(), businessId)) {
      throw new AlreadyExistsException("Department already exists");
    }

    Business business = businessRepository.findById(businessId)
        .orElseThrow(() -> new ResourceNotFoundException("Business not found"));


    Department department = new Department();

    department.setName(request.getName().trim());
    department.setDescription(request.getDescription());
    department.setBusiness(business);

    return departmentRepository.save(department);
  }


  @Override
  public Department updateDepartment(String slug, UpdateDepartmentRequest request) {

    Department department = getDepartment(slug);

    if (request.getName() != null && !request.getName().isBlank()) {

      departmentRepository.findByNameIgnoreCase(request.getName().trim())
          .ifPresent(existing -> {
            if (!existing.getSlug().equals(slug)) {
              throw new AlreadyExistsException("Department already exists");
            }
          });

      department.setName(request.getName().trim());
    }

    if (request.getDescription() != null) {
      department.setDescription(request.getDescription().trim());
    }

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
        .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
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
  @Transactional
  public void assignUsers(String departmentSlug, AssignUsersToDepartmentRequest request) {

    Department department = departmentRepository.findBySlug(departmentSlug)
        .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

    for (String userSlug : request.getUserSlugs()) {
      User user = userRepository.findBySlug(userSlug)
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));

      if (user.getDepartments().contains(department)) {
        throw new AlreadyExistsException(
            String.format(
                "User %s already belongs to department %s",
                user.getEmail(),
                department.getName()
            )
        );
      }

      user.getDepartments().add(department);

      userRepository.save(user);
    }
  }


  @Override
  @Transactional
  public void removeUser(String departmentSlug, String userSlug) {

    Department department = departmentRepository.findBySlug(departmentSlug)
        .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

    User user = userRepository.findBySlug(userSlug)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!user.getDepartments().contains(department)) {
      throw new ResourceNotFoundException(
          String.format(
              "User %s does not belong to department %s",
              user.getEmail(),
              department.getName()
          )
      );
    }

    user.getDepartments().remove(department);

    userRepository.save(user);
  }


  @Override
  @Transactional(readOnly = true)
  public List<UserResponseDto> getDepartmentUsers(String departmentSlug) {

    Department department = departmentRepository.findBySlug(departmentSlug)
        .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

    return department.getUsers()
        .stream()
        .map(userService::convertToDto)
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
