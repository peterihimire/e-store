package com.benkih.estore.auth.repository;

import com.benkih.estore.auth.entity.PasswordResetToken;
import com.benkih.estore.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findBySlug(String slug);

    List<PasswordResetToken> findByUserAndUsedAtIsNull(User user);

    Optional<PasswordResetToken> findTopByUserAndUsedAtIsNullOrderByCreatedAtDesc(User user);
}
