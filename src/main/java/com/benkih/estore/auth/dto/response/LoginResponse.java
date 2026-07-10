package com.benkih.estore.auth.dto.response;

import com.benkih.estore.user.dto.response.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
  private String token;
  private String refreshToken;
  private UserResponseDto user;
}
