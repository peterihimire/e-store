package com.benkih.estore.user.service;

import com.benkih.estore.user.dto.request.CreateUserRequest;
import com.benkih.estore.user.dto.request.UserUpdateRequest;
import com.benkih.estore.user.entity.User;

public interface IUserService {
  User getUserBySlug(String slug);
  User createUser(CreateUserRequest request);
  User updateUser(UserUpdateRequest request, String slug);
  void deleteUser(String slug);
}
