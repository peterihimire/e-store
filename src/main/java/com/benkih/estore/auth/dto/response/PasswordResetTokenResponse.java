package com.benkih.estore.auth.dto.response;

import com.benkih.estore.auth.entity.PasswordResetToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetTokenResponse {
  private String plainToken;
  private PasswordResetToken passwordReset;
}
