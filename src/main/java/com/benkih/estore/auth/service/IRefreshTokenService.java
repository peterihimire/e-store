package com.benkih.estore.auth.service;

import com.benkih.estore.auth.entity.RefreshToken;
import com.benkih.estore.user.entity.User;

public interface IRefreshTokenService {
  String createRefreshToken(User user, String device, String ipAddress);

  RefreshToken validateRefreshToken(String plainToken);

  String rotateRefreshToken(String plainToken, String device, String ipAddress);

  void revokeRefreshToken(String plainToken);

  void revokeAllUserTokens(User user);

  void deleteExpiredTokens();
}
