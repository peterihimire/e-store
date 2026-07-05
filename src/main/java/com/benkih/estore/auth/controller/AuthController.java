package com.benkih.estore.auth.controller;


import com.benkih.estore.auth.dto.request.LoginRequest;
import com.benkih.estore.auth.dto.request.SignupRequest;
import com.benkih.estore.auth.dto.response.LoginResponse;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.security.jwt.JwtUtils;
import com.benkih.estore.security.user.StoreUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request){
      //    try {
      Authentication authentication = authenticationManager
          .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      String jwt = jwtUtils.generateTokenForUser(authentication);
      StoreUserDetails userDetails = (StoreUserDetails) authentication.getPrincipal();
      LoginResponse jwtResponse = new LoginResponse(userDetails.getSlug(), jwt);
      return ResponseEntity.ok(new ApiResponse("success", "User login success", jwtResponse));
      //    } catch (AuthenticationException e) {
      //      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse("fail", "Invalid email or password", e.getMessage()));
      //    }
  }

//  @PostMapping("/sign-up")
//  public ResponseEntity<ApiResponse> signup(@Valid @RequestBody SignupRequest request){
//    Authentication authentication = authenticationManager
//        .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
//    SecurityContextHolder.getContext().setAuthentication(authentication);
//    String jwt = jwtUtils.generateTokenForUser(authentication);
//    StoreUserDetails userDetails = (StoreUserDetails) authentication.getPrincipal();
//    LoginResponse jwtResponse = new LoginResponse(userDetails.getSlug(), jwt);
//    return ResponseEntity.ok(new ApiResponse("success", "User login success", jwtResponse));
//  }
}
