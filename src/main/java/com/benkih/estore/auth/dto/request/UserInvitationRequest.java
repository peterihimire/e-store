package com.benkih.estore.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInvitationRequest {
  @NotBlank
  private String firstName;

  @NotBlank
  private String lastName;

  @Email
  @NotBlank
  private String email;

  private String phoneNumber;

  private Set<String> roleSlugs = new HashSet<>();

  private Set<String> departmentSlugs = new HashSet<>();

  @Size(max = 500)
  private String message;
}
