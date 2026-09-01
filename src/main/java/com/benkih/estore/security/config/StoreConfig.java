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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class StoreConfig {
  private final StoreUserDetailsService storeUserDetailsService;
  private final JwtAuthEntryPoint jwtAuthEntryPoint;
  private final AuthTokenFilter authTokenFilter;


  @Bean
  public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }


  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception{
    return authConfig.getAuthenticationManager();
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
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(jwtAuthEntryPoint)
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.setContentType("application/json");
              response.setStatus(HttpServletResponse.SC_FORBIDDEN);
              Map<String, Object> errorResponse = new HashMap<>();
              errorResponse.put("timestamp", Instant.now().toString());
              errorResponse.put("status", HttpServletResponse.SC_FORBIDDEN);
              errorResponse.put("error", "Forbidden");
              errorResponse.put("message", accessDeniedException.getMessage());
              errorResponse.put("path", request.getServletPath());
              new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
            })
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers("/api/v1/products/**").permitAll()
            .requestMatchers("/api/v1/users/**").permitAll()
            .requestMatchers("/api/v1/webhooks/**").permitAll()
            .requestMatchers("/api/v1/payments/callback/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .requestMatchers("/h2-console/**").permitAll()

            .requestMatchers("/api/v1/admin/**").authenticated()
            .requestMatchers("/api/v1/manager/**").authenticated()
            .requestMatchers("/api/v1/carts/**").authenticated()
            .requestMatchers("/api/v1/cartItems/**").authenticated()
            .requestMatchers("/api/v1/orders/**").authenticated()

            .anyRequest().authenticated()
        )
        .authenticationProvider(daoAuthenticationProvider())
        .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
        .formLogin(form -> form.disable())
        .httpBasic(httpBasic -> httpBasic.disable());

    return http.build();
  }


  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(List.of(
        "http://localhost:3000",      // React dev server
        "http://localhost:5173",      // Vite dev server
        "http://localhost:4200",      // Angular dev server
        "http://localhost:8080",      // Another frontend
        "https://yourdomain.com"      // Your production domain
    ));

    configuration.setAllowedMethods(List.of(
        "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
    ));

    configuration.setAllowedHeaders(List.of("*"));

    configuration.setAllowCredentials(true);

    configuration.setExposedHeaders(List.of(
        "Authorization",
        "Content-Disposition"
    ));

    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
