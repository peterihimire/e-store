package com.benkih.estore.auth.service;

import com.benkih.estore.auth.dto.response.VerificationTokenResponse;
import com.benkih.estore.auth.entity.EmailVerification;
import com.benkih.estore.auth.repository.EmailVerificationRepository;
import com.benkih.estore.user.entity.User;
import java.security.SecureRandom;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailVerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public VerificationTokenResponse createVerificationToken(User user) {
        // Invalidate previous unused tokens
        verificationRepository.invalidateAllUnusedTokens(user.getSlug());

        String plainToken = generateOtp();

        EmailVerification verification = new EmailVerification();
        verification.setUser(user);
        verification.setToken(passwordEncoder.encode(plainToken));
        verification.setExpiresAt(Instant.now().plusSeconds(10 * 60));

        verification = verificationRepository.save(verification);

        return new VerificationTokenResponse(plainToken, verification);
    }

    private String generateOtp() {
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }
}
