package com.benkih.estore.auth.service;

import com.benkih.estore.auth.dto.request.LoginRequest;
import com.benkih.estore.auth.dto.request.VerifyEmailRequest;
import com.benkih.estore.auth.dto.response.LoginResponse;
import com.benkih.estore.auth.dto.response.VerificationTokenResponse;
import com.benkih.estore.auth.entity.EmailVerification;
import com.benkih.estore.auth.repository.EmailVerificationRepository;
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
import com.benkih.estore.user.entity.Role;
import com.benkih.estore.user.repository.RoleRepository;
import com.benkih.estore.security.jwt.JwtUtils;
import com.benkih.estore.security.user.StoreUserDetails;
import com.benkih.estore.user.dto.request.CreateUserRequest;
import com.benkih.estore.user.dto.response.UserResponseDto;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.repository.UserRepository;
import com.benkih.estore.user.service.IUserService;
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

      VerificationTokenResponse token =
          verificationService.createVerificationToken(user);

      EmailRequest email =
          verificationEmailBuilder.build(user, token.getPlainToken());
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
  public LoginResponse verifyEmail(VerifyEmailRequest request) {

    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() ->
            new ResourceNotFoundException("User not found"));

    if (user.isEmailVerified()) {
      throw new BadRequestException("Email already verified.");
    }

    EmailVerification verification =
        verificationRepository.findTopByUserOrderByCreatedAtDesc(user)
            .orElseThrow(() ->
                new BadRequestException("Verification code not found."));

    if (verification.isExpired()) {
      throw new BadRequestException("Verification code expired.");
    }

    if (verification.isUsed()) {
      throw new BadRequestException("Verification code has already been used.");
    }

    if (!passwordEncoder.matches(
        request.getCode(),
        verification.getToken())) {

      throw new BadRequestException("Invalid verification code.");
    }

    verification.setUsedAt(LocalDateTime.now());

    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setEmailVerified(true);
    user.setStatus(UserStatus.ACTIVE);

    verificationRepository.save(verification);
    userRepository.save(user);
    notificationService.sendWelcomeEmail(user);

//    try {
//      EmailRequest email = welcomeEmailBuilder.build(user);
//      emailService.send(email);
//    } catch (Exception ex) {
//      log.error("Unable to send welcome email", ex);
//    }

    return createLoginResponse(user);
  }



@Transactional(readOnly = true)
@Override
public LoginResponse login(LoginRequest request) {

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

//  notificationService.sendLoginEmail(user);
//  try {
//    EmailRequest email = loginEmailBuilder.build(user);
//    emailService.send(email);
//  } catch (Exception ex) {
//    log.error("Unable to send login email", ex);
//  }

  return createLoginResponse(authentication, user);
}



private LoginResponse createLoginResponse(Authentication authentication, User user) {

  String jwt = jwtUtils.generateTokenForUser(authentication);

  StoreUserDetails userDetails =
      (StoreUserDetails) authentication.getPrincipal();

  UserResponseDto userDto = userService.convertToDto(user);

  return new LoginResponse(
      jwt,
      "",
      userDto
  );
}

  private LoginResponse createLoginResponse(User user) {

    StoreUserDetails userDetails =
        StoreUserDetails.buildUserDetails(user);

    Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    String jwt = jwtUtils.generateTokenForUser(authentication);
    UserResponseDto userDto = userService.convertToDto(user);

    return new LoginResponse(
        jwt,
        "",
        userDto
    );
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