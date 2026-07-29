package com.benkih.estore.auth.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.InvitationStatus;
import com.benkih.estore.department.entity.Department;
import com.benkih.estore.user.entity.Role;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Table(
    name = "user_invitations",
    indexes = {
        @Index(name = "idx_invitation_email", columnList = "email"),
        @Index(name = "idx_invitation_token", columnList = "tokenHash"),
        @Index(name = "idx_invitation_status", columnList = "status"),
        @Index(name = "idx_invitation_expiry", columnList = "expiresAt")
    }
)
public class UserInvitation extends BaseEntity {

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String tokenHash;

  @ManyToMany
  @JoinTable(
      name = "user_invitation_departments",
      joinColumns = @JoinColumn(name = "invitation_id"),
      inverseJoinColumns = @JoinColumn(name = "department_id")
  )
  private Set<Department> departments = new HashSet<>();

  @ManyToMany
  @JoinTable(
      name = "user_invitation_roles",
      joinColumns = @JoinColumn(name = "invitation_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Set<Role> roles = new HashSet<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private InvitationStatus status = InvitationStatus.PENDING;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  private LocalDateTime acceptedAt;

  private String createdBy;
  private String updatedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invited_by")
  private User invitedBy;

  public boolean isExpired() {
    return !expiresAt.isAfter(LocalDateTime.now());
  }
}