package com.benkih.estore.auth.controller;

import static org.springframework.http.HttpStatus.CONFLICT;

import com.benkih.estore.auth.dto.request.AcceptInvitationRequest;
import com.benkih.estore.auth.dto.request.ChangePasswordRequest;
import com.benkih.estore.auth.dto.request.ForgotPasswordRequest;
import com.benkih.estore.auth.dto.request.LoginRequest;
import com.benkih.estore.auth.dto.request.ResetPasswordRequest;
import com.benkih.estore.auth.dto.request.UserInvitationRequest;
import com.benkih.estore.auth.dto.request.VerifyEmailRequest;
import com.benkih.estore.auth.dto.response.LoginResponse;
import com.benkih.estore.auth.service.AuthService;
import com.benkih.estore.auth.service.UserInvitationService;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.security.tenant.TenantContext;
import com.benkih.estore.user.dto.request.CreateUserRequest;
import com.benkih.estore.user.dto.response.UserResponseDto;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Auth", description = "Login, registration, verification and password reset")
@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final UserInvitationService userInvitationService;
    private final TenantContext tenantContext;

    //  private final AuthenticationManager authenticationManager;
    //  private final JwtUtils jwtUtils;

    @SecurityRequirements(value = {})
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginResponse response = authService.login(request, httpRequest);

        return ResponseEntity.ok(new ApiResponse<>("success", "User login success", response));
    }

    @SecurityRequirements(value = {})
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        try {
            User user = authService.register(request);
            UserResponseDto userDto = userService.convertToDto(user);
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            "success",
                            "A code has been sent to your e-mail to verify your account",
                            userDto));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @SecurityRequirements(value = {})
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyEmail(
            @RequestBody VerifyEmailRequest request, HttpServletRequest httpRequest) {
        LoginResponse response = authService.verifyEmail(request, httpRequest);
        //   UserResponseDto userDto = userService.convertToDto(user);
        return ResponseEntity.ok(new ApiResponse<>("success", "User verified", response));
    }

    @SecurityRequirements(value = {})
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @RequestHeader("Refresh-Token") String refreshToken, HttpServletRequest request) {

        LoginResponse response = authService.refreshToken(refreshToken, request);

        return ResponseEntity.ok(
                new ApiResponse<>("success", "Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Refresh-Token") String refreshToken) {

        authService.logout(refreshToken);
        return ResponseEntity.ok(new ApiResponse<>("success", "Logout success", null));
    }

    @SecurityRequirements(value = {})
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "If an account with that email exists, a password reset code has been sent.",
                        null));
    }

    @SecurityRequirements(value = {})
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ResponseEntity.ok(new ApiResponse<>("success", "Password reset successful.", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(request);

        return ResponseEntity.ok(new ApiResponse<>("success", "Change password successful.", null));
    }

    @PreAuthorize("hasAuthority('USER_CREATE')")
    @PostMapping("/user-invitation")
    public ResponseEntity<ApiResponse<Void>> userInvitation(
            @Valid @RequestBody UserInvitationRequest request) {
        Long businessId = tenantContext.getBusinessId();

        userInvitationService.inviteUser(request, businessId);

        return ResponseEntity.ok(
                new ApiResponse<>("success", "User invitation sent successful.", null));
    }

    @SecurityRequirements(value = {})
    @PostMapping("/accept-invitation/{token}")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(
            @PathVariable String token, @Valid @RequestBody AcceptInvitationRequest request) {

        userInvitationService.acceptInvitation(token, request);

        return ResponseEntity.ok(
                new ApiResponse<>("success", "User invitation accept successful.", null));
    }
}
