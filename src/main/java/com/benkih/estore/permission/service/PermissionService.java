package com.benkih.estore.permission.service;

import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.permission.dto.response.PermissionResponseDto;
import com.benkih.estore.permission.entity.Permission;
import com.benkih.estore.permission.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService implements IPermissionService {
  private final PermissionRepository permissionRepository;


  @Override
  public List<Permission> getPermissions() {
    return permissionRepository.findAll();
  }


  @Override
  public List<PermissionResponseDto> getConvertedPermissions(List<Permission> permissions) {
    return permissions
        .stream()
        .map(this::convertToDto)
        .toList();
  }


  @Override
  public PermissionResponseDto getPermissionBySlug(String slug) {
    Permission permission = permissionRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));
    return convertToDto(permission);
  }


  @Override
  public PermissionResponseDto convertToDto(Permission permission) {
    return new PermissionResponseDto(
        permission.getSlug(),
        permission.getName(),
        permission.getDescription(),
        permission.getResource(),
        permission.getAction()
    );
  }
}
