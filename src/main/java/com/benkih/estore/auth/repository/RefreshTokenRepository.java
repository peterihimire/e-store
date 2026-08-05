package com.benkih.estore.auth.repository;

import com.benkih.estore.auth.entity.RefreshToken;
import com.benkih.estore.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findBySlug(String slug);

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  List<RefreshToken> findByUser(User user);

  void deleteByUser(User user);

  void deleteByExpiresAtBefore(LocalDateTime time);
}
