package com.benkih.estore.auth.dto.response;

import com.benkih.estore.auth.entity.EmailVerification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationTokenResponse {
  private String plainToken;
  private EmailVerification verification;
}
