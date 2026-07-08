package com.benkih.estore.auth.controller;

import com.benkih.estore.auth.dto.request.LoginRequest;
import com.benkih.estore.auth.dto.response.LoginResponse;
import com.benkih.estore.auth.service.AuthService;
import com.benkih.estore.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
//  private final AuthenticationManager authenticationManager;
//  private final JwtUtils jwtUtils;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request){
    LoginResponse response = authService.login(request);

    return ResponseEntity.ok(new ApiResponse("success", "User login success", response));
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
