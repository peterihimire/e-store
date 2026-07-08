package com.benkih.estore.auth.service;

import com.benkih.estore.auth.dto.request.LoginRequest;
import com.benkih.estore.auth.dto.response.LoginResponse;
import com.benkih.estore.email.builder.LoginEmailBuilder;
import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.service.EmailService;
import com.benkih.estore.security.jwt.JwtUtils;
import com.benkih.estore.security.user.StoreUserDetails;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final UserRepository userRepository;

  private final LoginEmailBuilder loginEmailBuilder;
  private final EmailService emailService;

  public LoginResponse login(LoginRequest request) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateTokenForUser(authentication);
    StoreUserDetails userDetails = (StoreUserDetails) authentication.getPrincipal();

    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() ->
            new UsernameNotFoundException("User not found"));

    try {
      EmailRequest email = loginEmailBuilder.build(user);
      emailService.send(email);
    } catch (Exception ex) {
      log.error("Unable to send login email", ex);
    }

    return new LoginResponse(
        userDetails.getSlug(),
        jwt
    );
  }
}
