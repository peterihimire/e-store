package com.benkih.estore.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {

  @NotBlank
  private String refreshToken;
}