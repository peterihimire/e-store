package com.benkih.estore.auth.dto.response;


import com.benkih.estore.common.enums.InvitationStatus;
import com.benkih.estore.department.dto.response.DepartmentResponseDto;
import com.benkih.estore.role.dto.response.RoleResponseDto;
import com.benkih.estore.user.dto.response.UserSummaryResponseDto;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInvitationResponseDto {

  private String slug;

  private String email;

  private String firstName;

  private String lastName;

  private String phoneNumber;

  private InvitationStatus status;

  private Instant expiresAt;

  private Instant acceptedAt;

  private UserSummaryResponseDto invitedBy;

  private List<RoleResponseDto> roles;

  private List<DepartmentResponseDto> departments;
}