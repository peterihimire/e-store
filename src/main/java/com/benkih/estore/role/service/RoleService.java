package com.benkih.estore.role.service;

import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.permission.entity.Permission;
import com.benkih.estore.permission.repository.PermissionRepository;
import com.benkih.estore.permission.service.PermissionService;
import com.benkih.estore.role.dto.request.CreateRoleRequest;
import com.benkih.estore.role.dto.request.UpdateRoleRequest;
import com.benkih.estore.role.dto.response.RoleResponseDto;
import com.benkih.estore.role.entity.Role;
import com.benkih.estore.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class RoleService implements IRoleService{
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final PermissionService permissionService;

  @Override
  public Role createRole(CreateRoleRequest request) {

    if (roleRepository.existsByNameIgnoreCase(request.getName())) {
      throw new AlreadyExistsException("Role already exists");
    }

    Role role = new Role();
    role.setName(request.getName());
    role.setDescription(request.getDescription());
    role.setSystemRole(false);
    role.setActive(true);

    Set<Permission> permissions = new HashSet<>(
            permissionRepository.findAllBySlugIn(request.getPermissionSlugs()));

    if (permissions.size() != request.getPermissionSlugs().size()) {
      throw new ResourceNotFoundException("One or more permissions do not exist");
    }
    role.setPermissions(permissions);

    return roleRepository.save(role);
  }

  @Override
  @Transactional
  public Role updateRole(String slug, UpdateRoleRequest request) {

    Role role = getRole(slug);

    if (role.isSystemRole()) {
      throw new BadRequestException("System roles cannot be modified");
    }

    // Update name
    if (request.getName() != null && !request.getName().isBlank()) {

      roleRepository.findByNameIgnoreCase(request.getName().trim())
          .ifPresent(existing -> {
            if (!existing.getSlug().equals(slug)) {
              throw new AlreadyExistsException("Role already exists");
            }
          });

      role.setName(request.getName().trim());
    }

    // Update description
    if (request.getDescription() != null) {
      role.setDescription(request.getDescription().trim());
    }

    // Update active status
    if (request.getActive() != null) {
      role.setActive(request.getActive());
    }

    // Update permissions
    if (request.getPermissionSlugs() != null) {

      Set<Permission> permissions = new HashSet<>(
          permissionRepository.findAllBySlugIn(request.getPermissionSlugs())
      );

      if (permissions.size() != request.getPermissionSlugs().size()) {
        throw new ResourceNotFoundException("One or more permissions do not exist");
      }

      role.getPermissions().clear();
      role.getPermissions().addAll(permissions);
    }

    return roleRepository.save(role);
  }

  @Override
  public void deleteRole(String slug) {
    Role role = getRole(slug);
    if (role.isSystemRole()) {
      throw new BadRequestException("System roles cannot be deleted");
    }

    roleRepository.delete(role);
  }

  @Override
  @Transactional(readOnly = true)
  public Role getRole(String slug) {

    return roleRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
  }

  @Override
  @Transactional(readOnly = true)
  public List<RoleResponseDto> getRoles() {

    return roleRepository.findAll()
        .stream()
        .map(this::convertToDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public RoleResponseDto convertToDto(Role role) {
    return new RoleResponseDto(
        role.getSlug(),
        role.getName(),
        role.isSystemRole(),
        role.isActive(),
        role.getDescription(),
        role.getPermissions()
            .stream()
            .map(permissionService::convertToDto)
            .toList()
    );
  }
}
