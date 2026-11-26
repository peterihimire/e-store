package com.benkih.estore.auth.service;

import com.benkih.estore.auth.dto.request.ChangePasswordRequest;
import com.benkih.estore.auth.dto.request.ForgotPasswordRequest;
import com.benkih.estore.auth.dto.request.LoginRequest;
import com.benkih.estore.auth.dto.request.ResetPasswordRequest;
import com.benkih.estore.auth.dto.request.VerifyEmailRequest;
import com.benkih.estore.auth.dto.response.LoginResponse;
import com.benkih.estore.user.dto.request.CreateUserRequest;
import com.benkih.estore.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    User register(CreateUserRequest request);

    LoginResponse verifyEmail(VerifyEmailRequest request, HttpServletRequest httpRequest);

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    void logout(String refreshToken);

    LoginResponse refreshToken(String refreshToken, HttpServletRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request);
}
