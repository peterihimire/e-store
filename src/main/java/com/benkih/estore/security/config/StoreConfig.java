package com.benkih.estore.security.config;

import com.benkih.estore.security.jwt.AuthTokenFilter;
import com.benkih.estore.security.jwt.JwtAuthEntryPoint;
import com.benkih.estore.security.user.StoreUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class StoreConfig {
  private final StoreUserDetailsService storeUserDetailsService;
  private final JwtAuthEntryPoint jwtAuthEntryPoint;
  private static final List<String> SECURED_URLS = List.of("/api/v1/carts/**","/api/v1/cartItems/**");

//  @Bean
//  public ModelMapper modelMapper(){
//    return new ModelMapper();
//  }
  @Bean
  public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthTokenFilter authTokenFilter(){
    return new AuthTokenFilter();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider(){
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(storeUserDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // Disable CSRF for stateless REST APIs
        .csrf(csrf -> csrf.disable()) //lambda expression[.csrf(csrf -> csrf.disable())] vs method reference[.csrf(AbstractHttpConfigurer::disable)]

        // Configure CORS (if needed)
        .cors(cors -> cors.configure(http))

        // Configure exception handling
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(jwtAuthEntryPoint)
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.setContentType("application/json");
              response.setStatus(HttpServletResponse.SC_FORBIDDEN);
              Map<String, Object> errorResponse = new HashMap<>();
              errorResponse.put("timestamp", LocalDateTime.now().toString());
              errorResponse.put("status", HttpServletResponse.SC_FORBIDDEN);
              errorResponse.put("error", "Forbidden");
              errorResponse.put("message", accessDeniedException.getMessage());
              errorResponse.put("path", request.getServletPath());
              new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
            })
        )

        // Set session management to stateless (for JWT)
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )

        // Configure authorization rules
        .authorizeHttpRequests(authz -> authz
            // Public endpoints - no authentication required
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // If using Swagger
            .requestMatchers("/h2-console/**").permitAll()  // If using H2 (development only)

            // Protected endpoints - authentication required
            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/v1/manager/**").hasAnyRole("ADMIN", "MANAGER")

            // All other requests require authentication
            .anyRequest().authenticated()
        )

        // Set authentication provider
        .authenticationProvider(daoAuthenticationProvider())

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        .addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class)

        // Disable default form login
        .formLogin(form -> form.disable())

        // Disable HTTP Basic authentication
        .httpBasic(httpBasic -> httpBasic.disable());

    return http.build();
  }
}
