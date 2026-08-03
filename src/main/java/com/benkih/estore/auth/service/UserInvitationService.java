package com.benkih.estore.auth.service;

import com.benkih.estore.auth.dto.request.AcceptInvitationRequest;
import com.benkih.estore.auth.dto.request.UserInvitationRequest;
import com.benkih.estore.auth.dto.response.UserInvitationResponseDto;
import com.benkih.estore.auth.entity.UserInvitation;
import com.benkih.estore.auth.repository.UserInvitationRepository;
import com.benkih.estore.common.enums.InvitationStatus;
import com.benkih.estore.common.enums.UserStatus;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.department.dto.response.DepartmentResponseDto;
import com.benkih.estore.department.entity.Department;
import com.benkih.estore.department.repository.DepartmentRepository;
import com.benkih.estore.email.service.EmailService;
import com.benkih.estore.notification.NotificationService;
import com.benkih.estore.role.dto.response.RoleResponseDto;
import com.benkih.estore.role.entity.Role;
import com.benkih.estore.role.repository.RoleRepository;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.user.dto.request.CreateUserCommand;
import com.benkih.estore.user.dto.response.UserSummaryResponseDto;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.repository.UserRepository;
import com.benkih.estore.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInvitationService implements IUserInvitationService{
  private final UserInvitationRepository userInvitationRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final DepartmentRepository departmentRepository;
  private final PasswordEncoder passwordEncoder;
  private final NotificationService notificationService;
  private final UserService userService;
  private final CurrentUserService currentUserService;

  @Override
  @Transactional
  public UserInvitationResponseDto inviteUser(UserInvitationRequest request) {

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new AlreadyExistsException("User already exists.");
    }

    if (userInvitationRepository.existsByEmailIgnoreCaseAndStatus(
        request.getEmail(),
        InvitationStatus.PENDING)) {

      throw new AlreadyExistsException("A pending invitation already exists for this email.");
    }

    Set<Role> roles = new HashSet<>(
        roleRepository.findAllBySlugIn(request.getRoleSlugs()));

    if (roles.size() != request.getRoleSlugs().size()) {
      throw new ResourceNotFoundException("One or more roles do not exist.");
    }

    Set<Department> departments = new HashSet<>(
        departmentRepository.findAllBySlugIn(request.getDepartmentSlugs()));

    if (departments.size() != request.getDepartmentSlugs().size()) {
      throw new ResourceNotFoundException("One or more departments do not exist.");
    }

    String plainToken = UUID.randomUUID().toString();

    UserInvitation invitation = new UserInvitation();

    invitation.setEmail(request.getEmail());
    invitation.setFirstName(request.getFirstName());
    invitation.setLastName(request.getLastName());
    invitation.setPhoneNumber(request.getPhoneNumber());
    invitation.setRoles(roles);
    invitation.setInvitedBy(currentUserService.getCurrentUser());
    invitation.setDepartments(departments);
    invitation.setTokenHash(passwordEncoder.encode(plainToken));
    invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
    invitation = userInvitationRepository.save(invitation);

    notificationService.sendInvitationEmail(invitation, plainToken);

    return convertToDto(invitation);
  }

  @Override
  public List<UserInvitationResponseDto> getInvitations() {

    return userInvitationRepository.findAll()
        .stream()
        .map(this::convertToDto)
        .toList();
  }

  @Override
  public UserInvitationResponseDto getInvitation(String slug) {
    UserInvitation invitation = userInvitationRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation not found."));

    return convertToDto(invitation);
  }

  @Override
  public void cancelInvitation(String slug) {
    UserInvitation invitation = userInvitationRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation not found."));

    if (invitation.isAccepted()) {
      throw new BadRequestException("Invitation has already been accepted.");
    }

    invitation.revoke();

    userInvitationRepository.save(invitation);
  }

  @Override
  public void resendInvitation(String slug) {
    UserInvitation invitation = userInvitationRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation not found."));

    if (invitation.isAccepted()) {
      throw new BadRequestException("Invitation already accepted.");
    }

    String plainToken = UUID.randomUUID().toString();
    invitation.setTokenHash(passwordEncoder.encode(plainToken));
    invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
    invitation.setStatus(InvitationStatus.PENDING);

    userInvitationRepository.save(invitation);

    notificationService.sendInvitationEmail(invitation, plainToken);

  }

  @Override
  public void acceptInvitation(String token, AcceptInvitationRequest request) {
    if (!request.getPassword().equals(request.getConfirmPassword())) {

      throw new BadRequestException("Passwords do not match.");

    }

    UserInvitation invitation = userInvitationRepository.findAllByStatus(InvitationStatus.PENDING)
            .stream()
            .filter(i -> passwordEncoder.matches(token, i.getTokenHash()))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Invalid invitation " +
                "token."));

    if (invitation.isExpired()) {
      throw new BadRequestException("Invitation has expired.");
    }

    if (userRepository.existsByEmail(invitation.getEmail())) {
      throw new AlreadyExistsException("User already exists.");
    }

    CreateUserCommand command = CreateUserCommand.builder()
            .email(invitation.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhoneNumber())
            .password(request.getPassword())
            .roles(invitation.getRoles())
            .departments(invitation.getDepartments())
            .status(UserStatus.ACTIVE)
            .emailVerified(true)
            .build();

    User user = userService.createUserCommand(command);

    invitation.accept();

    userInvitationRepository.save(invitation);

    notificationService.sendWelcomeEmail(user);

  }

  @Override
  public UserInvitationResponseDto convertToDto(UserInvitation invitation) {

    UserInvitationResponseDto dto = new UserInvitationResponseDto();

    dto.setSlug(invitation.getSlug());
    dto.setEmail(invitation.getEmail());
    dto.setFirstName(invitation.getFirstName());
    dto.setLastName(invitation.getLastName());
    dto.setPhoneNumber(invitation.getPhoneNumber());
    dto.setStatus(invitation.getStatus());
    dto.setExpiresAt(invitation.getExpiresAt());
    dto.setAcceptedAt(invitation.getAcceptedAt());

    if (invitation.getInvitedBy() != null) {
      dto.setInvitedBy(
          new UserSummaryResponseDto(
              invitation.getInvitedBy().getSlug(),
              invitation.getInvitedBy().getFirstName(),
              invitation.getInvitedBy().getLastName(),
              invitation.getInvitedBy().getEmail()
          )
      );
    }

    dto.setRoles(
        invitation.getRoles()
            .stream()
            .map(role -> new RoleResponseDto(
                role.getSlug(),
                role.getName(),
                role.isSystemRole(),
                role.isActive(),
                role.getDescription(),
                List.of()
            ))
            .toList());

    dto.setDepartments(
        invitation.getDepartments()
            .stream()
            .map(dept -> new DepartmentResponseDto(
                dept.getSlug(),
                dept.getName(),
                dept.getDescription()
            ))
            .toList());

    return dto;
  }
}
