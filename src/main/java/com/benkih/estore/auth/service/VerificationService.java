package com.benkih.estore.auth.service;

import com.benkih.estore.auth.dto.response.VerificationTokenResponse;
import com.benkih.estore.auth.entity.EmailVerification;
import com.benkih.estore.auth.repository.EmailVerificationRepository;
import com.benkih.estore.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificationService implements IVerificationService {
  private final EmailVerificationRepository verificationRepository;
  private final PasswordEncoder passwordEncoder;


  @Override
  @Transactional
  public VerificationTokenResponse createVerificationToken(User user) {
    // Invalidate previous unused tokens
    verificationRepository.invalidateAllUnusedTokens(user.getSlug());

    String plainToken = generateOtp();

    EmailVerification verification = new EmailVerification();
    verification.setUser(user);
    verification.setToken(passwordEncoder.encode(plainToken));
    verification.setExpiresAt(Instant.now().plusSeconds(10*60));

    verification = verificationRepository.save(verification);

    return new VerificationTokenResponse(
        plainToken,
        verification
    );
  }


  private String generateOtp() {
    SecureRandom random = new SecureRandom();
    int otp = 100000 + random.nextInt(900000);
    return String.valueOf(otp);
  }
}
