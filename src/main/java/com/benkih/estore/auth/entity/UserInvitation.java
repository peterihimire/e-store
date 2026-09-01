package com.benkih.estore.auth.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.InvitationStatus;
import com.benkih.estore.department.entity.Department;
import com.benkih.estore.role.entity.Role;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(
    name = "user_invitations",
    indexes = {
        @Index(name = "idx_invitation_email", columnList = "email"),
        @Index(name = "idx_invitation_token", columnList = "tokenHash"),
        @Index(name = "idx_invitation_status", columnList = "status"),
        @Index(name = "idx_invitation_expiry", columnList = "expiresAt"),
        @Index(name = "idx_invitation_business_id", columnList = "business_id")
    }
)
public class UserInvitation extends AuditableEntity {

  @Column(nullable = false)
  private String email;

  private String firstName;

  private String lastName;

  private String phoneNumber;

  @Column(nullable = false, unique = true)
  private String tokenHash;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "user_invitation_departments",
      joinColumns = @JoinColumn(name = "invitation_id"),
      inverseJoinColumns = @JoinColumn(name = "department_id")
  )
  private Set<Department> departments = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
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
  private Instant expiresAt;

  private Instant acceptedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invited_by")
  private User invitedBy;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

  public boolean isExpired() {
    return !expiresAt.isAfter(Instant.now());
  }

  public boolean isPending() {
    return status == InvitationStatus.PENDING;
  }

  public boolean isAccepted() {
    return status == InvitationStatus.ACCEPTED;
  }

  public void accept() {
    if (isExpired()) {
      throw new IllegalStateException("Invitation has expired.");
    }

    if (isAccepted()) {
      throw new IllegalStateException("Invitation already accepted.");
    }

    status = InvitationStatus.ACCEPTED;
    acceptedAt = Instant.now();
  }

  public void revoke() {
    if (isAccepted()) {
      throw new IllegalStateException("Accepted invitation cannot be revoked.");
    }

    status = InvitationStatus.CANCELLED;
  }
}