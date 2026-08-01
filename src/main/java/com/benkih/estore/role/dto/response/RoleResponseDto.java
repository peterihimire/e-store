package com.benkih.estore.role.dto.response;

import com.benkih.estore.role.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponseDto {

  private String slug;
  private String name;
  private boolean systemRole;
  private boolean active;

  public static RoleResponseDto fromEntity(Role role) {
    return new RoleResponseDto(
        role.getSlug(),
        role.getName(),
        role.isSystemRole(),
        role.isActive()
    );
  }
}