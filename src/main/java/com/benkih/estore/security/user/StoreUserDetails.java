package com.benkih.estore.security.user;

import com.benkih.estore.business.entity.BusinessMember;
import com.benkih.estore.business.enums.MemberStatus;
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
import java.util.HashSet;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class StoreUserDetails implements UserDetails {
  private Long userId;
  private String slug;
  private String email;
  private String password;
  private Collection<GrantedAuthority> authorities;



  public static StoreUserDetails buildUserDetails(User user) {

    Collection<GrantedAuthority> authorities = new HashSet<>();

    // System-level authorities
    user.getRoles()
        .stream()
        .filter(Role::isActive)
        .flatMap(role -> role.getPermissions().stream())
        .map(permission ->
            new SimpleGrantedAuthority(permission.getName())
        )
        .forEach(authorities::add);

    // Business-level authorities
    BusinessMember businessMember = user.getBusinessMember();

    if (businessMember != null
        && businessMember.getStatus() == MemberStatus.ACTIVE
        && businessMember.getRole() != null
        && businessMember.getRole().isActive()) {

      businessMember.getRole()
          .getPermissions()
          .stream()
          .map(permission ->
              new SimpleGrantedAuthority(permission.getName())
          )
          .forEach(authorities::add);
    }

    StoreUserDetails userDetails = new StoreUserDetails();

    userDetails.userId = user.getId();
    userDetails.slug = user.getSlug();
    userDetails.email = user.getEmail();
    userDetails.password = user.getPassword();
    userDetails.authorities = authorities;

    return userDetails;
  }
//  public static StoreUserDetails buildUserDetails(User user){
//    Collection<GrantedAuthority> authorities = user.getRoles()
//            .stream()
//            .filter(Role::isActive)
//            .flatMap(role -> role.getPermissions().stream())
//            .map(permission -> new SimpleGrantedAuthority(permission.getName()))
//            .collect(Collectors.toSet());
//
//    StoreUserDetails userDetails = new StoreUserDetails();
//    userDetails.userId = user.getId();
//    userDetails.slug = user.getSlug();
//    userDetails.email = user.getEmail();
//    userDetails.password = user.getPassword();
//    userDetails.authorities = authorities;
//
//    return userDetails;
//
////    log.info("User {} has authorities: {}", user.getEmail(), authorities);
////    return new StoreUserDetails(
////        user.getId(),
////        user.getSlug(),
////        user.getEmail(),
////        user.getPassword(),
////        authorities
////    );
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
