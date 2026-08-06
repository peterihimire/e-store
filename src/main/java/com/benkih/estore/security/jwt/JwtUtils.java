package com.benkih.estore.security.jwt;

import com.benkih.estore.security.user.StoreUserDetails;
import com.benkih.estore.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtUtils {

  @Value("${auth.token.access-secret}")
  private String accessSecret;

  @Value("${auth.token.refresh-secret}")
  private String refreshSecret;

  @Value("${auth.token.access-expiration-ms}")
  private long accessExpiration;

  @Value("${auth.token.refresh-expiration-ms}")
  private long refreshExpiration;

  /*
   * =========================
   * ACCESS TOKEN
   * =========================
   */

  public String generateAccessToken(Authentication authentication) {

    StoreUserDetails principal = (StoreUserDetails) authentication.getPrincipal();

    Date now = new Date();

    return Jwts.builder()
        .setSubject(principal.getEmail())
        .claim("slug", principal.getSlug())
//        .claim("roles", roles)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + accessExpiration))
        .signWith(accessKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  /*
   * =========================
   * REFRESH TOKEN
   * =========================
   */

  public String generateRefreshToken(User user) {

    Date now = new Date();

    return Jwts.builder()
        .setId(UUID.randomUUID().toString())     // JWT ID
        .setSubject(user.getEmail())
        .claim("slug", user.getSlug())
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + refreshExpiration))
        .signWith(refreshKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  /*
   * =========================
   * VALIDATION
   * =========================
   */

  public boolean validateAccessToken(String token) {

    try {

      Jwts.parserBuilder()
          .setSigningKey(accessKey())
          .build()
          .parseClaimsJws(token);

      return true;

    } catch (
        ExpiredJwtException |
        UnsupportedJwtException |
        MalformedJwtException |
        SignatureException |
        IllegalArgumentException ex) {

      throw new JwtException(ex.getMessage());
    }
  }

  public boolean validateRefreshToken(String token) {

    try {

      Jwts.parserBuilder()
          .setSigningKey(refreshKey())
          .build()
          .parseClaimsJws(token);

      return true;

    } catch (
        ExpiredJwtException |
        UnsupportedJwtException |
        MalformedJwtException |
        SignatureException |
        IllegalArgumentException ex) {

      throw new JwtException(ex.getMessage());
    }
  }

  /*
   * =========================
   * CLAIM EXTRACTION
   * =========================
   */

  public String getUsernameFromAccessToken(String token) {

    return Jwts.parserBuilder()
        .setSigningKey(accessKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public String getUsernameFromRefreshToken(String token) {

    return Jwts.parserBuilder()
        .setSigningKey(refreshKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public String getSlugFromRefreshToken(String token) {

    return Jwts.parserBuilder()
        .setSigningKey(refreshKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .get("slug", String.class);
  }

  public String getJtiFromRefreshToken(String token) {

    return Jwts.parserBuilder()
        .setSigningKey(refreshKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getId();
  }

  public Date getExpirationFromRefreshToken(String token) {

    return Jwts.parserBuilder()
        .setSigningKey(refreshKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getExpiration();
  }

  public String getRefreshTokenId(String token) {

    return Jwts.parserBuilder()
        .setSigningKey(refreshKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getId();
  }

  /*
   * =========================
   * KEYS
   * =========================
   */

  private Key accessKey() {
    return Keys.hmacShaKeyFor(
        Decoders.BASE64.decode(accessSecret)
    );
  }

  private Key refreshKey() {
    return Keys.hmacShaKeyFor(
        Decoders.BASE64.decode(refreshSecret)
    );
  }
}