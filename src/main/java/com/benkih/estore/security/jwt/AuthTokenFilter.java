package com.benkih.estore.security.jwt;

import com.benkih.estore.security.user.StoreUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {
  private final JwtUtils jwtUtils;
  private final StoreUserDetailsService storeUserDetailsService;


  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    try {
      String jwt = parseJwt(request);
      if(StringUtils.hasText(jwt) && jwtUtils.validateAccessToken(jwt)){
        String username = jwtUtils.getUsernameFromAccessToken(jwt);
        UserDetails userDetails = storeUserDetailsService.loadUserByUsername(username);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    } catch (JwtException e) {
      log.warn("JWT validation failed: {}", e.getMessage());
      SecurityContextHolder.clearContext();
    } catch (Exception e) {
      log.error("Authentication error: {}", e.getMessage());
      SecurityContextHolder.clearContext();
    }
    filterChain.doFilter(request, response);
  }


  private String parseJwt(HttpServletRequest request){
    String headerAuth = request.getHeader("Authorization");
    if(StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")){
      return headerAuth.substring(7);
    }
    return null;
  }
}
