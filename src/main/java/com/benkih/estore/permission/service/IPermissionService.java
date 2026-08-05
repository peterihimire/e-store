package com.benkih.estore.permission.service;

import com.benkih.estore.permission.dto.response.PermissionResponseDto;
import com.benkih.estore.permission.entity.Permission;

import java.util.List;

public interface IPermissionService {

  List<Permission> getPermissions();

  List<PermissionResponseDto> getConvertedPermissions(List<Permission> permissions);

  PermissionResponseDto getPermissionBySlug(String slug);

  PermissionResponseDto convertToDto(Permission permission);
}
