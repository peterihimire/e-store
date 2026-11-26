package com.benkih.estore.auth.service;

import com.benkih.estore.auth.dto.response.PasswordResetTokenResponse;
import com.benkih.estore.auth.entity.PasswordResetToken;
import com.benkih.estore.auth.repository.PasswordResetTokenRepository;
import com.benkih.estore.user.entity.User;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    @Override
    public PasswordResetTokenResponse createPasswordResetToken(User user) {

        // Revoke previous unused tokens
        passwordResetTokenRepository
                .findByUserAndUsedAtIsNull(user)
                .forEach(token -> token.setUsedAt(Instant.now()));

        String plainToken = generateOtp();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(passwordEncoder.encode(plainToken));
        resetToken.setExpiresAt(Instant.now().plus(Duration.ofMinutes(15)));

        resetToken = passwordResetTokenRepository.save(resetToken);

        return new PasswordResetTokenResponse(plainToken, resetToken);
    }

    private String generateOtp() {
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }
}
