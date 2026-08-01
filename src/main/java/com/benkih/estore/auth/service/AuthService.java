package com.benkih.estore.auth.service;

import com.benkih.estore.auth.dto.request.*;
import com.benkih.estore.auth.dto.response.LoginResponse;
import com.benkih.estore.auth.dto.response.PasswordResetTokenResponse;
import com.benkih.estore.auth.dto.response.VerificationTokenResponse;
import com.benkih.estore.auth.entity.EmailVerification;
import com.benkih.estore.auth.entity.PasswordResetToken;
import com.benkih.estore.auth.entity.RefreshToken;
import com.benkih.estore.auth.repository.EmailVerificationRepository;
import com.benkih.estore.auth.repository.PasswordResetTokenRepository;
import com.benkih.estore.common.enums.RoleName;
import com.benkih.estore.common.enums.UserStatus;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.email.builder.LoginEmailBuilder;
import com.benkih.estore.email.builder.VerificationEmailBuilder;
import com.benkih.estore.email.builder.WelcomeEmailBuilder;
import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.service.EmailService;
import com.benkih.estore.notification.INotificationService;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.role.entity.Role;
import com.benkih.estore.role.repository.RoleRepository;
import com.benkih.estore.security.jwt.JwtUtils;
import com.benkih.estore.security.user.StoreUserDetails;
import com.benkih.estore.user.dto.request.CreateUserRequest;
import com.benkih.estore.user.dto.response.UserResponseDto;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.repository.UserRepository;
import com.benkih.estore.user.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final UserRepository userRepository;

  private final LoginEmailBuilder loginEmailBuilder;
  private final EmailService emailService;

  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;
  private final WelcomeEmailBuilder welcomeEmailBuilder;
  private final VerificationEmailBuilder verificationEmailBuilder;
  private final VerificationService verificationService;
  private final EmailVerificationRepository verificationRepository;
  private final IUserService userService;
  private final INotificationService notificationService;
  private final RefreshTokenService refreshTokenService;
  private final IPasswordResetTokenService passwordResetTokenService;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final CurrentUserService currentUserService;



  @Transactional
  @Override
  public User register(CreateUserRequest request) {
    Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
    if (existingUser.isPresent()) {
      User user = existingUser.get();

      if (user.isEmailVerified()) {
        throw new AlreadyExistsException(
            request.getEmail() + " already exists!"
        );
      }

      VerificationTokenResponse token = verificationService.createVerificationToken(user);
      // wrap in try-catch or use the notification service
      EmailRequest email = verificationEmailBuilder.build(user, token.getPlainToken());
      emailService.send(email);

      return user;
    }

    Role customerRole = roleRepository.findByName(RoleName.CUSTOMER.name())
        .orElseThrow(() ->
            new RuntimeException("Default role not found: " + RoleName.CUSTOMER));

    User user = new User();

    user.setEmail(request.getEmail());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());

    user.setStatus(UserStatus.PENDING_VERIFICATION);
    user.setEmailVerified(false);
    user.setRoles(Set.of(customerRole));

    user = userRepository.save(user);

    VerificationTokenResponse token = verificationService.createVerificationToken(user);
    notificationService.sendVerificationEmail(user, token.getPlainToken());
//    try {
//      EmailRequest email =
//          verificationEmailBuilder.build(user, token.getPlainToken());
//      emailService.send(email);
//    } catch (Exception ex) {
//      log.error("Unable to send verification email", ex);
//    }
    return user;
  }

  @Transactional
  @Override
  public LoginResponse verifyEmail(VerifyEmailRequest request, HttpServletRequest httpRequest) {

    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() ->
            new ResourceNotFoundException("User not found"));

    if (user.isEmailVerified()) {
      throw new BadRequestException("Email already verified.");
    }

    EmailVerification verification = verificationRepository.findTopByUserOrderByCreatedAtDesc(user)
            .orElseThrow(() -> new BadRequestException("Verification code not found."));

    if (verification.isExpired()) {
      throw new BadRequestException("Verification code expired.");
    }

    if (verification.isUsed()) {
      throw new BadRequestException("Verification code has already been used.");
    }

    if (!passwordEncoder.matches(request.getCode(), verification.getToken())) {
      throw new BadRequestException("Invalid verification code.");
    }

    verification.setUsedAt(LocalDateTime.now());

    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setEmailVerified(true);
    user.setStatus(UserStatus.ACTIVE);

    verificationRepository.save(verification);
    userRepository.save(user);

    String device = httpRequest.getHeader("User-Agent");
    String ipAddress = httpRequest.getRemoteAddr();
    notificationService.sendWelcomeEmail(user);

    return createLoginResponse(user, device, ipAddress);
  }



@Transactional
@Override
public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {

  Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(
          request.getEmail(),
          request.getPassword()
      )
  );

  SecurityContextHolder.getContext().setAuthentication(authentication);

  User user = userRepository.findByEmail(request.getEmail())
      .orElseThrow(() ->
          new UsernameNotFoundException("User not found"));

  if (!user.isEmailVerified()) {
    throw new BadRequestException("Please verify your email before logging in.");
  }

  if (user.getStatus() != UserStatus.ACTIVE) {
    throw new BadRequestException("Your account is not active.");
  }

  String device = httpRequest.getHeader("User-Agent");
  String ipAddress = httpRequest.getRemoteAddr();
//  notificationService.sendLoginEmail(user);

  refreshTokenService.revokeAllUserTokens(user);
//  String refreshToken = refreshTokenService.createRefreshToken(user, device, ipAddress);

  return createLoginResponse(authentication, user, device, ipAddress);
}


  @Override
  @Transactional
  public void logout(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      SecurityContextHolder.clearContext();
      return;
    }

    refreshTokenService.revokeRefreshToken(refreshToken);
    SecurityContextHolder.clearContext();
  }

  @Override
  @Transactional
  public LoginResponse refreshToken(
      String refreshToken,
      HttpServletRequest request) {

    RefreshToken storedToken = refreshTokenService.validateRefreshToken(refreshToken);

    User user = storedToken.getUser();

    StoreUserDetails userDetails = StoreUserDetails.buildUserDetails(user);

    Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authentication);

    String device = request.getHeader("User-Agent");
    String ip = request.getRemoteAddr();
    String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken, device, ip);
    String accessToken = jwtUtils.generateAccessToken(authentication);

    UserResponseDto dto = userService.convertToDto(user);

    return new LoginResponse(accessToken, newRefreshToken, dto);
  }

  @Transactional
  @Override
  public void forgotPassword(ForgotPasswordRequest request){
  Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

  if (optionalUser.isEmpty()) {
    return;
  }

  User user = optionalUser.get();

  if (!user.isEmailVerified()) {
    return;
  }

  PasswordResetTokenResponse token = passwordResetTokenService.createPasswordResetToken(user);
  notificationService.sendPasswordReset(user, token.getPlainToken());
}

  @Transactional
  @Override
  public void resetPassword(ResetPasswordRequest request) {

    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() ->
            new ResourceNotFoundException("User not found"));

    PasswordResetToken resetToken = passwordResetTokenRepository
        .findTopByUserAndUsedAtIsNullOrderByCreatedAtDesc(user)
        .orElseThrow(() -> new BadRequestException("Password reset code not found."));

    if (resetToken.isExpired()) {
      throw new BadRequestException("Password reset code has expired.");
    }

    if (resetToken.isUsed()) {
      throw new BadRequestException("Password reset code has already been used.");
    }

    if (!passwordEncoder.matches(request.getToken(), resetToken.getTokenHash())) {
      throw new BadRequestException("Invalid password reset code.");
    }

//    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
//      throw new BadRequestException("Passwords do not match.");
//    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    resetToken.setUsedAt(LocalDateTime.now());
    passwordResetTokenRepository.save(resetToken);

    refreshTokenService.revokeAllUserTokens(user);
  }

  @Transactional
  @Override
  public void changePassword(ChangePasswordRequest request) {
    User user = currentUserService.getCurrentUser();

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new BadRequestException("Current password is incorrect.");
    }

//    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
//      throw new BadRequestException("Passwords do not match.");
//    }

    if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
      throw new BadRequestException("New password must be different from your current password.");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    refreshTokenService.revokeAllUserTokens(user);
  }


private LoginResponse createLoginResponse(
    Authentication authentication,
    User user,
    String device,
    String ipAddress) {

//  String jwt = jwtUtils.generateAccessToken(authentication);
  String accessToken = jwtUtils.generateAccessToken(authentication);

  String refreshToken = refreshTokenService.createRefreshToken(user, device, ipAddress);
  StoreUserDetails userDetails = (StoreUserDetails) authentication.getPrincipal();
  UserResponseDto userDto = userService.convertToDto(user);

  return new LoginResponse(accessToken,refreshToken, userDto);
}

  private LoginResponse createLoginResponse(
      User user,
      String device,
      String ipAddress) {
    StoreUserDetails userDetails = StoreUserDetails.buildUserDetails(user);

    Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );

    SecurityContextHolder.getContext().setAuthentication(authentication);
    return createLoginResponse(
        authentication,
        user,
        device,
        ipAddress
    );

//    String jwt = jwtUtils.generateAccessToken(authentication);
//    UserResponseDto userDto = userService.convertToDto(user);
//
//    return new LoginResponse(
//        jwt,
//        "",
//        userDto
//    );
  }
}

//  @Override
//  public LoginResponse login(LoginRequest request) {
//    Authentication authentication = authenticationManager.authenticate(
//        new UsernamePasswordAuthenticationToken(
//            request.getEmail(),
//            request.getPassword()
//        )
//    );
//
//    SecurityContextHolder.getContext().setAuthentication(authentication);
//    String jwt = jwtUtils.generateTokenForUser(authentication);
//    StoreUserDetails userDetails = (StoreUserDetails) authentication.getPrincipal();
//
//    User user = userRepository.findByEmail(request.getEmail())
//        .orElseThrow(() ->
//            new UsernameNotFoundException("User not found"));
//    if (!user.isEmailVerified()) {
//      throw new BadRequestException("Please verify your email before logging in.");
//    }
//
//    if (user.getStatus() != UserStatus.ACTIVE) {
//      throw new BadRequestException("Your account is not active.");
//    }
////    UserResponseDto userDto = userService.convertToDto(user);
////    String refreshToken = "yet to work on the feature";
//    try {
//      EmailRequest email = loginEmailBuilder.build(user);
//      emailService.send(email);
//    } catch (Exception ex) {
//      log.error("Unable to send login email", ex);
//    }
//
//    return new LoginResponse(
//        jwt,
//      userDetails.getSlug(),
//    );
//  }


//  @Transactional
//  public LoginResponse verifyEmail(VerifyEmailRequest request) {
//
//    User user = userRepository.findByEmail(request.getEmail())
//        .orElseThrow(() ->
//            new ResourceNotFoundException("User not found"));
//
//    if (user.isEmailVerified()) {
//      throw new BadRequestException("Email already verified.");
//    }
//
//    EmailVerification verification =
//        verificationRepository.findTopByUserOrderByCreatedAtDesc(user)
//            .orElseThrow(() ->
//                new BadRequestException("Verification code not found."));
//
//
////    if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
////      throw new BadRequestException("Verification code expired.");
////    }
//
//    if (verification.isExpired()) {
//      throw new BadRequestException("Verification code expired.");
//    }
//
//    if (!passwordEncoder.matches(
//        request.getCode(),
//        verification.getToken())) {
//
//      throw new BadRequestException("Invalid verification code.");
//    }
//
////    if (verification.getUsedAt() != null) {
////      throw new BadRequestException("Verification code has already been used.");
////    }
////
//    if(verification.isUsed()){
//      throw new BadRequestException("Verification code has already been used.");
//    }
//    verification.setUsedAt(LocalDateTime.now());
//
//    user.setEmailVerified(true);
//    user.setStatus(UserStatus.ACTIVE);
//
//    verificationRepository.save(verification);
//    userRepository.save(user);
//
//    String accessToken = jwtService.generateAccessToken(user);
//    String jwt = jwtUtils.generateTokenForUser(authentication);
////    String refreshToken = refreshTokenService.createRefreshToken(user);
//
//
//    try {
//      EmailRequest email = welcomeEmailBuilder.build(user);
//      emailService.send(email);
//    } catch (Exception ex) {
//      log.error("Unable to send welcome email", ex);
//    }
//  }

//  @Transactional
//  @Override
//  public User register(CreateUserRequest request) {
//    Set<Role> defaultRoles = new HashSet<>();
//
//    // Add ROLE_CUSTOMER
//    Role customerRole = roleRepository.findByName(RoleName.CUSTOMER.name())
//        .orElseThrow(() -> new RuntimeException("Default role not found: " + RoleName.CUSTOMER));
//
//    defaultRoles.add(customerRole);
//
//    User existingUser = userRepository.findByEmail(request.getEmail()).get();
//
//    if (existingUser && )) {
//      throw new AlreadyExistsException(request.getEmail() + " already exists!");
//    }
//
//    User user = new User();
//
//    user.setEmail(request.getEmail());
//    user.setPassword(passwordEncoder.encode(request.getPassword()));
//    user.setFirstName(request.getFirstName());
//    user.setLastName(request.getLastName());
//    user.setRoles(defaultRoles);
//    user = userRepository.save(user);
//    log.info("User info detail={}", user);
//
//    try {
//      EmailRequest email = welcomeEmailBuilder.build(user);
//      emailService.send(email);
//    } catch (Exception ex) {
//      log.error("Unable to send welcome email", ex);
//    }
//
//    return user;
//  }