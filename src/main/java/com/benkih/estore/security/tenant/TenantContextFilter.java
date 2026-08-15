package com.benkih.estore.security.tenant;

import com.benkih.estore.security.user.StoreUserDetails;
import com.benkih.estore.user.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {
  private final TenantContext tenantContext;
  private final TenantResolver tenantResolver;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    try {
      Authentication authentication = SecurityContextHolder
              .getContext()
              .getAuthentication();

      if (authentication != null
          && authentication.isAuthenticated()
          && authentication.getPrincipal() instanceof StoreUserDetails principal) {

        Long userId = principal.getUserId();

        Long businessId = tenantResolver.resolve(userId);

        if (businessId != null) {
          tenantContext.setBusinessId(businessId);
        }
      }

      filterChain.doFilter(request, response);

    } finally {
      tenantContext.clear();
    }
  }

  private Long getAuthenticatedUserId(Authentication authentication) {
    Object principal = authentication.getPrincipal();

    if (principal instanceof User user) {
      return user.getId();
    }

    throw new IllegalStateException("Authenticated principal is not a User");
  }
}
