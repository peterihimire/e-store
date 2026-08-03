package com.benkih.estore.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptInvitationRequest {

  private String firstName;

  private String lastName;

  private String phoneNumber;

  @NotBlank
  @Size(min = 8)
  private String password;

  @NotBlank
  private String confirmPassword;
}
