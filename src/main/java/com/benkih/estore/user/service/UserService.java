package com.benkih.estore.user.service;

import com.benkih.estore.user.dto.request.CreateUserRequest;
import com.benkih.estore.user.dto.request.UserUpdateRequest;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
  private final UserRepository userRepository;
  @Override
  public User getUserBySlug(String slug) {
    return null;
  }

  @Override
  public User createUser(CreateUserRequest request) {
    return null;
  }

  @Override
  public User updateUser(UserUpdateRequest request, String slug) {
    return null;
  }

  @Override
  public void deleteUser(String slug) {

  }
}
