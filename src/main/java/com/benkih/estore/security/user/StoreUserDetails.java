package com.benkih.estore.security.user;

import com.benkih.estore.role.entity.Role;
import com.benkih.estore.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class StoreUserDetails implements UserDetails {
  private String slug;
  private String email;
  private String password;

  private Collection<GrantedAuthority> authorities;

  public static StoreUserDetails buildUserDetails(User user){
    Collection<GrantedAuthority> authorities =
        user.getRoles()
            .stream()
            .filter(Role::isActive)
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> new SimpleGrantedAuthority(permission.getName()))
            .collect(Collectors.toSet());

    log.info("User {} has authorities: {}", user.getEmail(), authorities);
    //    List<GrantedAuthority> authorities = user.getRoles()
    //        .stream()
    //        .map(role -> new SimpleGrantedAuthority(role.getName()))
    //        .collect(Collectors.toList());

    return new StoreUserDetails(
        user.getSlug(),
        user.getEmail(),
        user.getPassword(),
        authorities
    );
  }

//  public static StoreUserDetails buildUserDetails(User user){
//    List<GrantedAuthority> authorities = user.getRoles()
//        .stream()
//        .map(role -> {
//          String roleName = role.getName();
//          // Add ROLE_ prefix if not already present
//          if (!roleName.startsWith("ROLE_")) {
//            roleName = "ROLE_" + roleName;
//          }
//          log.debug("Adding authority: {}", roleName);
//          return new SimpleGrantedAuthority(roleName);
//        })
//        .collect(Collectors.toList());
//
//    log.info("User {} has authorities: {}", user.getEmail(), authorities);
//    //    List<GrantedAuthority> authorities = user.getRoles()
//    //        .stream()
//    //        .map(role -> new SimpleGrantedAuthority(role.getName()))
//    //        .collect(Collectors.toList());
//
//    return new StoreUserDetails(
//        user.getSlug(),
//        user.getEmail(),
//        user.getPassword(),
//        authorities
//    );
//  }
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return UserDetails.super.isAccountNonExpired();
  }

  @Override
  public boolean isAccountNonLocked() {
    return UserDetails.super.isAccountNonLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return UserDetails.super.isCredentialsNonExpired();
  }

  @Override
  public boolean isEnabled() {
    return UserDetails.super.isEnabled();
  }
}
