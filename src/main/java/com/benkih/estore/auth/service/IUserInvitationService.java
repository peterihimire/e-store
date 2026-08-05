package com.benkih.estore.auth.service;

import com.benkih.estore.auth.dto.request.AcceptInvitationRequest;
import com.benkih.estore.auth.dto.request.UserInvitationRequest;
import com.benkih.estore.auth.dto.response.UserInvitationResponseDto;
import com.benkih.estore.auth.entity.UserInvitation;

import java.util.List;

public interface IUserInvitationService {

  UserInvitationResponseDto inviteUser(UserInvitationRequest request);

  List<UserInvitationResponseDto> getInvitations();

  UserInvitationResponseDto getInvitation(String slug);

  void cancelInvitation(String slug);

  void resendInvitation(String slug);

  void acceptInvitation(String token, AcceptInvitationRequest request);

  UserInvitationResponseDto convertToDto(UserInvitation invitation);
}