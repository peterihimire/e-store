package com.benkih.estore.auth.service;

import com.benkih.estore.auth.entity.RefreshToken;
import com.benkih.estore.auth.repository.RefreshTokenRepository;
import com.benkih.estore.common.enums.RevocationReason;
import com.benkih.estore.security.jwt.JwtUtils;
import com.benkih.estore.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements IRefreshTokenService{
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtUtils jwtUtils;
  private final PasswordEncoder passwordEncoder;


  @Override
  public String createRefreshToken(User user, String device, String ipAddress) {
    String plainToken = jwtUtils.generateRefreshToken(user);
    Date expiration = jwtUtils.getExpirationFromRefreshToken(plainToken);

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setSlug(jwtUtils.getRefreshTokenId(plainToken));
    refreshToken.setUser(user);
    refreshToken.setTokenHash(hashToken(plainToken));
//    refreshToken.setTokenHash(passwordEncoder.encode(plainToken));
    refreshToken.setDevice(device);
    refreshToken.setIpAddress(ipAddress);
    refreshToken.setExpiresAt(expiration.toInstant().atZone(ZoneId.systemDefault()).toInstant());
    refreshTokenRepository.save(refreshToken);

    return plainToken;
  }


  @Override
  public RefreshToken validateRefreshToken(String plainToken) {
    jwtUtils.validateRefreshToken(plainToken);

    String tokenId = jwtUtils.getRefreshTokenId(plainToken);

    RefreshToken token = refreshTokenRepository.findBySlug(tokenId)
        .orElseThrow(() ->
            new IllegalArgumentException("Invalid refresh token"));

    if (token.isRevoked()) {
      throw new IllegalArgumentException("Refresh token revoked");
    }

    if (token.isExpired()) {
      throw new IllegalArgumentException("Refresh token expired");
    }

    String incomingHash = hashToken(plainToken);
    if (!incomingHash.equals(token.getTokenHash())) {
      throw new IllegalArgumentException("Invalid refresh token");
    }

    token.setLastUsedAt(Instant.now());

    return refreshTokenRepository.save(token);
  }


  @Override
  public String rotateRefreshToken(String plainToken, String device, String ipAddress) {
    RefreshToken existing = validateRefreshToken(plainToken);

    existing.setRevoked(true);
    existing.setRevokedAt(Instant.now());
    existing.setRevokedReason(RevocationReason.ROTATED);
    refreshTokenRepository.save(existing);

    return createRefreshToken(existing.getUser(), device, ipAddress);
  }


  @Override
  @Transactional
  public void revokeRefreshToken(String plainToken) {
    try {
      String tokenId = jwtUtils.getRefreshTokenId(plainToken);

      RefreshToken token = refreshTokenRepository
          .findBySlug(tokenId)
          .orElse(null);

      if (token == null) {
        return;
      }

      if (!token.isRevoked()) {
        token.setRevoked(true);
        token.setRevokedAt(Instant.now());
        token.setRevokedReason(RevocationReason.LOGOUT);
        refreshTokenRepository.save(token);
      }
    } catch(Exception ex){
      log.debug("Ignoring invalid refresh token during logout", ex);
    }
  }


  @Transactional
  @Override
  public void revokeAllUserTokens(User user) {
    refreshTokenRepository.findByUser(user)
        .forEach(token -> {
          token.setRevoked(true);
          token.setRevokedAt(Instant.now());
          token.setRevokedReason(RevocationReason.LOGOUT_ALL_DEVICES);
        });
  }


  @Override
  public void deleteExpiredTokens() {
    refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
  }


  private String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

      StringBuilder hex = new StringBuilder();
      for (byte b : hash) {
        hex.append(String.format("%02x", b));
      }

      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
