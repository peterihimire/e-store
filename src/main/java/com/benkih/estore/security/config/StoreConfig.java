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
//  private static final List<String> SECURED_URLS = List.of("/api/v1/carts/**","/api/v1/cartItems/**");

//  @Bean
//  public ModelMapper modelMapper(){
//    return new ModelMapper();
//  }
  @Bean
  public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }

//  @Bean
//  public AuthTokenFilter authTokenFilter(){
//    return new AuthTokenFilter();
//  }

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
        // Disable CSRF for stateless REST APIs
        .csrf(csrf -> csrf.disable()) //lambda expression[.csrf(csrf -> csrf.disable())] vs method reference[.csrf(AbstractHttpConfigurer::disable)]

        // Configure CORS (if needed)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // .cors(cors -> cors.configure(http))

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
            .requestMatchers("/api/v1/products/**").permitAll()
            .requestMatchers("/api/v1/users/**").permitAll()
            .requestMatchers("/api/v1/webhooks/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // If using Swagger
            .requestMatchers("/h2-console/**").permitAll()  // If using H2 (development only)

            // Protected endpoints - authentication required
            .requestMatchers("/api/v1/admin/**").authenticated()
            .requestMatchers("/api/v1/manager/**").authenticated()

            // 3. Protected endpoints (authenticated)
            // .requestMatchers("/api/v1/users/**", "/api/v1/profile/**").authenticated()
            .requestMatchers("/api/v1/carts/**").authenticated()
            .requestMatchers("/api/v1/cartItems/**").authenticated()
            .requestMatchers("/api/v1/orders/**").authenticated()

            // All other requests require authentication
            .anyRequest().authenticated()
        )

        // Set authentication provider
        .authenticationProvider(daoAuthenticationProvider())

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)

        // Disable default form login
        .formLogin(form -> form.disable())

        // Disable HTTP Basic authentication
        .httpBasic(httpBasic -> httpBasic.disable());

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Allow specific origins (your frontend URLs)
    configuration.setAllowedOrigins(List.of(
        "http://localhost:3000",      // React dev server
        "http://localhost:5173",      // Vite dev server
        "http://localhost:4200",      // Angular dev server
        "http://localhost:8080",      // Another frontend
        "https://yourdomain.com"      // Your production domain
    ));

    // Allow specific HTTP methods
    configuration.setAllowedMethods(List.of(
        "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
    ));

    // Allow all headers
    configuration.setAllowedHeaders(List.of("*"));

    // Allow credentials (cookies, authorization headers)
    configuration.setAllowCredentials(true);

    // Expose headers to frontend
    configuration.setExposedHeaders(List.of(
        "Authorization",
        "Content-Disposition"
    ));

    // Max age of preflight requests (in seconds)
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
