package com.benkih.estore.auth.service;

import com.benkih.estore.auth.dto.request.*;
import com.benkih.estore.auth.dto.response.LoginResponse;
import com.benkih.estore.user.dto.request.CreateUserRequest;
import com.benkih.estore.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

public interface IAuthService {

  @Transactional
  User register(CreateUserRequest request);

  @Transactional
  LoginResponse verifyEmail(VerifyEmailRequest request,
                            HttpServletRequest httpRequest);

  LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

  void logout(String refreshToken);

  LoginResponse refreshToken(
      String refreshToken,
      HttpServletRequest request
  );

  @Transactional
  void forgotPassword( ForgotPasswordRequest request);

  //  @Transactional
  void resetPassword( ResetPasswordRequest request);

  @Transactional
  void changePassword(ChangePasswordRequest request);
}
