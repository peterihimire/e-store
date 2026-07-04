package com.benkih.estore.user.service;

import com.benkih.estore.user.dto.request.CreateUserRequest;
import com.benkih.estore.user.dto.request.UserUpdateRequest;
import com.benkih.estore.user.dto.response.UserResponseDto;
import com.benkih.estore.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

public interface IUserService {
  @Transactional(readOnly = true)
  User getUserBySlug(String slug);

  @Transactional(readOnly = true)
  UserResponseDto getUserDtoBySlug(String slug);

  User createUser(CreateUserRequest request);
  User updateUser(UserUpdateRequest request, String slug);
  void deleteUser(String slug);

  UserResponseDto convertToDto(User user);

  User getAuthenticatedUser();
}
