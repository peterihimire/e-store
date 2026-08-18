package com.benkih.estore.role.service;

import com.benkih.estore.role.dto.request.CreateRoleRequest;
import com.benkih.estore.role.dto.request.UpdateRoleRequest;
import com.benkih.estore.role.dto.response.RoleResponseDto;
import com.benkih.estore.role.entity.Role;

import java.util.List;

public interface IRoleService {

  Role createRole(CreateRoleRequest request, Long businessId);

  Role updateRole(String slug, UpdateRoleRequest request);

  void deleteRole(String slug);

  Role getRole(String slug);

  List<RoleResponseDto> getRoles(Long businessId);

  List<RoleResponseDto> getRolesAdmin();

  RoleResponseDto convertToDto(Role role);

}