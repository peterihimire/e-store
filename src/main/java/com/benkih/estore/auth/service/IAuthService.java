package com.benkih.estore.auth.service;

import com.benkih.estore.auth.dto.request.LoginRequest;
import com.benkih.estore.auth.dto.request.VerifyEmailRequest;
import com.benkih.estore.auth.dto.response.LoginResponse;
import com.benkih.estore.user.dto.request.CreateUserRequest;
import com.benkih.estore.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

public interface IAuthService {

  @Transactional
  User register(CreateUserRequest request);

  @Transactional
  LoginResponse verifyEmail(VerifyEmailRequest request);

  LoginResponse login(LoginRequest request);
}
