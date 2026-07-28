package com.benkih.estore.auth.controller;

import com.benkih.estore.auth.dto.request.*;
import com.benkih.estore.auth.dto.response.LoginResponse;
import com.benkih.estore.auth.service.AuthService;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.response.ApiResponse;

import com.benkih.estore.security.user.StoreUserDetails;
import com.benkih.estore.user.dto.request.CreateUserRequest;
import com.benkih.estore.user.dto.response.UserResponseDto;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CONFLICT;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final IUserService userService;
//  private final AuthenticationManager authenticationManager;
//  private final JwtUtils jwtUtils;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest){
    LoginResponse response = authService.login(request, httpRequest);

    return ResponseEntity.ok(new ApiResponse("success", "User login success", response));
  }

  @PostMapping("/register")
  public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody CreateUserRequest request){
    try {
      User user = authService.register(request);
      UserResponseDto userDto = userService.convertToDto(user);
      return ResponseEntity.ok(new ApiResponse("success", "A code has been sent to your e-mail to verify your account", userDto ));
    } catch (AlreadyExistsException e) {
      return ResponseEntity.status(CONFLICT).body(new ApiResponse("fail", e.getMessage(), null));
    }
  }

  @PostMapping("/verify-email")
  public ResponseEntity<ApiResponse> verifyEmail( @RequestBody VerifyEmailRequest request, HttpServletRequest httpRequest){
    LoginResponse response = authService.verifyEmail(request, httpRequest);
    //   UserResponseDto userDto = userService.convertToDto(user);
      return ResponseEntity.ok(new ApiResponse("success", "User verified",
          response));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse> refreshToken(
      @RequestHeader("Refresh-Token") String refreshToken,
      HttpServletRequest request) {

    LoginResponse response = authService.refreshToken(refreshToken, request);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Token refreshed successfully",
            response
        )
    );
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse> logout(@RequestHeader("Refresh-Token") String refreshToken) {

    authService.logout(refreshToken);
    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Logout success",
            null
        )
    );
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {

    authService.forgotPassword(request);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "If an account with that email exists, a password reset code has been sent.",
            null
        )
    );
  }

  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

    authService.resetPassword(request);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Password reset successful.",
            null
        )
    );
  }

  @PostMapping("/change-password")
  public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {

    authService.changePassword(request);

    return ResponseEntity.ok(
        new ApiResponse(
            "success",
            "Change password successful.",
            null
        )
    );
  }

}
