package com.benkih.estore.common.config;

import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final UserRepository userRepository;
//  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {

    if (userRepository.count() > 0) {
      return;
    }

    createUser("Peter", "Ihimire", "peter@example.com");
    createUser("John", "Doe", "john@example.com");
    createUser("Jane", "Doe", "jane@example.com");
    createUser("Michael", "Smith", "michael@example.com");
    createUser("Sarah", "Jones", "sarah@example.com");
  }

  private void createUser(String firstName,
                          String lastName,
                          String email) {

    User user = new User();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    user.setPassword("password123");
//    user.setPassword(passwordEncoder.encode("password123"));

    userRepository.save(user);
  }
}
