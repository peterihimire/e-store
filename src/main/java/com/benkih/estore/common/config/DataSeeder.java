package com.benkih.estore.common.config;

import com.benkih.estore.user.entity.Role;
import com.benkih.estore.user.repository.RoleRepository;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Component
@RequiredArgsConstructor
@Transactional
public class DataSeeder implements CommandLineRunner {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

  @Override
  public void run(String... args) {
    try {
      log.info("Starting data seeding...");

      //       // Always create roles if they don't exist
//             createDefaultRoles();

      //      // Only create users if none exist
      //      if (userRepository.count() == 0) {
      //        createUsers();
      //        log.info("Data seeding completed successfully!");
      //      } else {
      //        log.info("Users already exist. Skipping user creation.");
      //      }

    } catch (Exception e) {
      log.error(" Error during data seeding: {}", e.getMessage(), e);
    }
  }

  private void createDefaultRoles() {
    Set<String> defaultRoles = Set.of("ADMIN", "CUSTOMER", "MANAGER");

    defaultRoles.forEach(roleName -> {
      if (roleRepository.findByName(roleName).isEmpty()) {
        Role role = new Role();
        role.setName(roleName);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        roleRepository.save(role);
        log.info("Created role: {}", roleName);
      }
    });
  }

  private void createUsers() {
    // Admin user
    createUser(
        "Peter",
        "Ihimire",
        "peter@example.com",
        Set.of("ADMIN", "CUSTOMER", "MANAGER")
    );

    // Customer users
    createUser("John", "Doe", "john@example.com", Set.of("CUSTOMER"));
    createUser("Jane", "Doe", "jane@example.com", Set.of("CUSTOMER"));
    createUser("Michael", "Smith", "michael@example.com", Set.of("MANAGER"));
    createUser("Sarah", "Jones", "sarah@example.com", Set.of("CUSTOMER"));
    createUser("Robert", "Wilson", "robert@example.com", Set.of("CUSTOMER"));
    createUser("Emily", "Brown", "emily@example.com", Set.of("CUSTOMER"));
  }

  private void createUser(String firstName, String lastName, String email, Set<String> roleNames) {
    try {
      // Check if user already exists
      if (userRepository.findByEmail(email).isPresent()) {
        log.warn("User with email {} already exists. Skipping.", email);
        return;
      }

      // Create user
      User user = new User();
      user.setFirstName(firstName);
      user.setLastName(lastName);
      user.setEmail(email);
      user.setPassword(passwordEncoder.encode("password123"));
      user.setCreatedAt(LocalDateTime.now());
      user.setUpdatedAt(LocalDateTime.now());

      // Assign roles
      Set<Role> roles = new HashSet<>();
      roleNames.forEach(roleName -> {
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        roles.add(role);
      });
      user.setRoles(roles);

      userRepository.save(user);
      log.info(" Created user: {} {} (email: {}) with roles: {}",
          firstName, lastName, email, roleNames);

    } catch (Exception e) {
      log.error(" Failed to create user {}: {}", email, e.getMessage());
    }
  }
}