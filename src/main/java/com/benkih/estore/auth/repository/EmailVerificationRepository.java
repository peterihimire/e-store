package com.benkih.estore.auth.repository;

import com.benkih.estore.auth.entity.EmailVerification;
import com.benkih.estore.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByUserOrderByCreatedAtDesc(User user);

    @Modifying
    @Query(
            """
        update EmailVerification ev
        set ev.usedAt = CURRENT_INSTANT
        where ev.user.slug = :userSlug
        and ev.usedAt is null
    """)
    void invalidateAllUnusedTokens(String userSlug);
}
