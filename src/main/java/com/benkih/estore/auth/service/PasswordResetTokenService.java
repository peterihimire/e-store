package com.benkih.estore.auth.service;

import com.benkih.estore.auth.dto.response.PasswordResetTokenResponse;
import com.benkih.estore.user.entity.User;

public interface PasswordResetTokenService {
    PasswordResetTokenResponse createPasswordResetToken(User user);
}
