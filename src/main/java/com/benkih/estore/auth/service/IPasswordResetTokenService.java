package com.benkih.estore.auth.service;

import com.benkih.estore.auth.dto.response.PasswordResetTokenResponse;
import com.benkih.estore.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

public interface IPasswordResetTokenService {
  @Transactional
  PasswordResetTokenResponse createPasswordResetToken(User user);
}
