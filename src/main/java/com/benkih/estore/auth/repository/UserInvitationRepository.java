package com.benkih.estore.auth.repository;


import com.benkih.estore.auth.entity.UserInvitation;
import com.benkih.estore.common.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserInvitationRepository extends JpaRepository<UserInvitation, Long> {

  Optional<UserInvitation> findBySlug(String slug);

  Optional<UserInvitation> findByTokenHash(String tokenHash);

  Optional<UserInvitation> findByEmailIgnoreCaseAndStatus(
      String email,
      InvitationStatus status
  );

  boolean existsByEmailIgnoreCaseAndStatus(
      String email,
      InvitationStatus status
  );

  List<UserInvitation> findAllByStatus(InvitationStatus status);

  List<UserInvitation> findAllByExpiresAtBeforeAndStatus(
      LocalDateTime dateTime,
      InvitationStatus status
  );
}