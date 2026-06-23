package com.benkih.estore.user.dto.request;

import lombok.Data;

@Data
public class CreateUserRequest {
  private String firstName;
  private String LastName;
  private String email;
  private String password;
}
