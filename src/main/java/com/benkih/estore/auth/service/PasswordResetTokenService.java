package com.benkih.estore.auth.service;

import com.benkih.estore.auth.dto.response.PasswordResetTokenResponse;
import com.benkih.estore.auth.entity.PasswordResetToken;
import com.benkih.estore.auth.repository.PasswordResetTokenRepository;
import com.benkih.estore.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetTokenService implements IPasswordResetTokenService {
  private final PasswordEncoder passwordEncoder;
  private final PasswordResetTokenRepository passwordResetTokenRepository;


  @Transactional
  @Override
  public PasswordResetTokenResponse createPasswordResetToken(User user) {

    // Revoke previous unused tokens
    passwordResetTokenRepository
        .findByUserAndUsedAtIsNull(user)
        .forEach(token ->
            token.setUsedAt(LocalDateTime.now())
        );

    String plainToken = generateOtp();

    PasswordResetToken resetToken = new PasswordResetToken();
    resetToken.setUser(user);
    resetToken.setTokenHash(passwordEncoder.encode(plainToken));
    resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));

   resetToken = passwordResetTokenRepository.save(resetToken);

    return new PasswordResetTokenResponse(
        plainToken,
        resetToken
    );
  }


  private String generateOtp() {
    SecureRandom random = new SecureRandom();
    int otp = 100000 + random.nextInt(900000);
    return String.valueOf(otp);
  }

}
