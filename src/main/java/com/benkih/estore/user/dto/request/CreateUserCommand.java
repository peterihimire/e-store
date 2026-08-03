package com.benkih.estore.user.dto.request;


import com.benkih.estore.common.enums.UserStatus;
import com.benkih.estore.department.entity.Department;
import com.benkih.estore.role.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class CreateUserCommand {

  private String email;

  private String firstName;

  private String lastName;

  private String phoneNumber;

  private String password;

  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  @Builder.Default
  private Set<Department> departments = new HashSet<>();

  private UserStatus status;

  private boolean emailVerified;
}