package com.benkih.estore.user.dto.request;

import com.benkih.estore.business.dto.request.BusinessRegistrationRequest;
import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.enums.AccountType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateUserRequest {

  @NotBlank(message = "First name is required")
  @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
  private String lastName;

  @NotBlank(message = "Email is required")
  @Email(message = "Please provide a valid email address")
  private String email;

  @NotNull(message = "Account type is required")
  private AccountType accountType;

  private  BusinessRegistrationRequest business;

}
// instead of @Pattern we could create a custom validation annotation: [@StrongPassword] in the future