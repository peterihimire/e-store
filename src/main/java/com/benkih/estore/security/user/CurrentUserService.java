package com.benkih.estore.security.user;

import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.common.exceptions.UnauthorizedException;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
  private final UserRepository userRepository;

  public StoreUserDetails getPrincipal() {
    Authentication authentication =
        SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null ||
        !(authentication.getPrincipal() instanceof StoreUserDetails principal)) {
      throw new UnauthorizedException("User not authenticated");
    }
    return principal;
  }

  public String getCurrentUserSlug() {
    return getPrincipal().getSlug();
  }

  public String getCurrentUserEmail() {
    return getPrincipal().getUsername(); // email
  }

  public User getCurrentUser() {
    return userRepository.findBySlug(getCurrentUserSlug())
        .orElseThrow(() ->
            new ResourceNotFoundException("User not found"));
  }
}