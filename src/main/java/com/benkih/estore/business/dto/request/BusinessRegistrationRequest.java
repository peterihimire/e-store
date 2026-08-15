package com.benkih.estore.business.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
public class BusinessRegistrationRequest {

  @NotBlank(message = "Business name is required")
  private String name;

  @Email(message = "Please provide a valid business email")
  private String email;

  private String phoneNumber;
}
