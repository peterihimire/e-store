package com.benkih.estore.auth.service;

import com.benkih.estore.auth.dto.response.VerificationTokenResponse;
import com.benkih.estore.user.entity.User;


public interface IVerificationService {
  VerificationTokenResponse createVerificationToken(User user);
}