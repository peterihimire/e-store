package com.benkih.estore.common.config;

import com.benkih.estore.common.enums.UserStatus;
import com.benkih.estore.permission.entity.Permission;
import com.benkih.estore.permission.repository.PermissionRepository;
import com.benkih.estore.role.entity.Role;
import com.benkih.estore.role.repository.RoleRepository;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Transactional
public class DataSeeder implements CommandLineRunner {
  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final PermissionRepository permissionRepository;


  @Override
  public void run(String... args) {
    try {
      log.info("Starting data seeding...");
//
//      // Always create roles if they don't exist
//      createDefaultRoles();
//
//      // Only create users if none exist
//      if (userRepository.count() == 0) {
//        createUsers();
//        log.info("Data seeding completed successfully!");
//      } else {
//        log.info("Users already exist. Skipping user creation.");
//      }
//      seedPermissions();
//      seedSystemRoles();
//      createSuperAdmin();

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

  private void seedPermissions() {

    createPermission("USER_CREATE", "USER", "CREATE");
    createPermission("USER_READ", "USER", "READ");
    createPermission("USER_UPDATE", "USER", "UPDATE");
    createPermission("USER_DELETE", "USER", "DELETE");

    createPermission("ROLE_CREATE", "ROLE", "CREATE");
    createPermission("ROLE_READ", "ROLE", "READ");
    createPermission("ROLE_UPDATE", "ROLE", "UPDATE");
    createPermission("ROLE_DELETE", "ROLE", "DELETE");

    createPermission("DEPARTMENT_CREATE", "DEPARTMENT", "CREATE");
    createPermission("DEPARTMENT_READ", "DEPARTMENT", "READ");
    createPermission("DEPARTMENT_UPDATE", "DEPARTMENT", "UPDATE");
    createPermission("DEPARTMENT_DELETE", "DEPARTMENT", "DELETE");

    createPermission("PRODUCT_CREATE", "PRODUCT", "CREATE");
    createPermission("PRODUCT_READ", "PRODUCT", "READ");
    createPermission("PRODUCT_UPDATE", "PRODUCT", "UPDATE");
    createPermission("PRODUCT_DELETE", "PRODUCT", "DELETE");

    createPermission("ORDER_CREATE", "ORDER", "CREATE");
    createPermission("ORDER_READ", "ORDER", "READ");
    createPermission("ORDER_UPDATE", "ORDER", "UPDATE");
    createPermission("ORDER_DELETE", "ORDER", "DELETE");
    createPermission("ORDER_CANCEL", "ORDER", "CANCEL");
    createPermission("ORDER_APPROVE", "ORDER", "APPROVE");
    createPermission("ORDER_REJECT", "ORDER", "REJECT");

    createPermission("PAYMENT_READ", "PAYMENT", "READ");
    createPermission("PAYMENT_UPDATE", "PAYMENT", "UPDATE");

    createPermission("REFUND_CREATE", "REFUND", "CREATE");
    createPermission("REFUND_READ", "REFUND", "READ");
    createPermission("REFUND_UPDATE", "REFUND", "UPDATE");
    createPermission("REFUND_APPROVE", "REFUND", "APPROVE");
    createPermission("REFUND_REJECT", "REFUND", "REJECT");
    createPermission("REFUND_CANCEL", "REFUND", "CANCEL");

    createPermission("INVENTORY_READ", "INVENTORY", "READ");
    createPermission("INVENTORY_ADJUST", "INVENTORY", "ADJUST");
    createPermission("INVENTORY_DAMAGE", "INVENTORY", "DAMAGE");

    createPermission("CATEGORY_CREATE", "CATEGORY", "CREATE");
    createPermission("CATEGORY_READ", "CATEGORY", "READ");
    createPermission("CATEGORY_UPDATE", "CATEGORY", "UPDATE");
    createPermission("CATEGORY_DELETE", "CATEGORY", "DELETE");

    createPermission("CUSTOMER_CREATE", "CUSTOMER", "CREATE");
    createPermission("CUSTOMER_READ", "CUSTOMER", "READ");
    createPermission("CUSTOMER_UPDATE", "CUSTOMER", "UPDATE");
    createPermission("CUSTOMER_DELETE", "CUSTOMER", "DELETE");

    createPermission("BUSINESS_CREATE", "BUSINESS", "CREATE");
    createPermission("BUSINESS_READ", "BUSINESS", "READ");
    createPermission("BUSINESS_UPDATE", "BUSINESS", "UPDATE");
    createPermission("BUSINESS_DELETE", "BUSINESS", "DELETE");

    createPermission("REPORT_READ", "REPORT", "READ");

    createPermission("SHIPPING_CREATE", "SHIPPING", "CREATE");
    createPermission("SHIPPING_READ", "SHIPPING", "READ");
    createPermission("SHIPPING_UPDATE", "SHIPPING", "UPDATE");
    createPermission("SHIPPING_DELETE", "SHIPPING", "DELETE");



    log.info("Permissions seeded.");
  }

  private void createPermission(String name, String resource, String action) {
    if (permissionRepository.findByName(name).isPresent()) {
      return;
    }

    Permission permission = new Permission();

    permission.setName(name);
    permission.setResource(resource);
    permission.setAction(action);
    permission.setDescription(name.replace("_", " "));
    permission.setCreatedAt(LocalDateTime.now());

    permissionRepository.save(permission);

    log.info("Created permission {}", name);
  }

  private void seedSystemRoles() {

      List<Permission> allPermissions = permissionRepository.findAll();

      seedSuperAdminRole(allPermissions);
      seedOwnerRole(allPermissions);

//    if (roleRepository.findByName("SUPER_ADMIN").isPresent()) {
//      return;
//    }
//
//    Role role = new Role();
//
//    role.setName("SUPER_ADMIN");
//    role.setSystemRole(true);
//    role.setActive(true);
//    role.setCreatedAt(LocalDateTime.now());
//
//    Set<Permission> permissions = new HashSet<>(permissionRepository.findAll());
//
//    role.setPermissions(permissions);
//
//    roleRepository.save(role);
//
//    log.info("Created SUPER_ADMIN role.");
  }

  private void createSuperAdmin() {
    if (userRepository.findByEmail("admin@estore.com").isPresent()) {
      return;
    }

    User user = new User();

    user.setFirstName("Super");
    user.setLastName("Admin");
    user.setEmail("admin@estore.com");
    user.setPassword(passwordEncoder.encode("Company22_"));
    user.setEmailVerified(true);
    user.setStatus(UserStatus.ACTIVE);

    user.setCreatedAt(LocalDateTime.now());

    Role superAdmin = roleRepository.findByName("SUPER_ADMIN")
        .orElseThrow();

    user.setRoles(Set.of(superAdmin));

    userRepository.save(user);

    log.info("Super Admin created.");
  }

  private void seedSuperAdminRole(List<Permission> permissions) {

    if (roleRepository.findByName("SUPER_ADMIN").isPresent()) {
      return;
    }

    Role role = new Role();

    role.setName("SUPER_ADMIN");
    role.setSystemRole(true);
    role.setActive(true);
    role.setCreatedAt(LocalDateTime.now());

    role.setPermissions(new HashSet<>(permissions));

    roleRepository.save(role);

    log.info("Created SUPER_ADMIN role.");
  }

  private void seedOwnerRole(List<Permission> allPermissions) {

    if (roleRepository.findByName("OWNER").isPresent()) {
      return;
    }

    Set<String> ownerResources = Set.of(
        "USER",
        "ROLE",
        "DEPARTMENT",
        "PRODUCT",
        "CATEGORY",
        "ORDER",
        "PAYMENT",
        "REFUND",
        "INVENTORY",
        "CUSTOMER",
        "REPORT"
    );

    Set<Permission> permissions = allPermissions.stream()
        .filter(permission ->
            ownerResources.contains(permission.getResource())
        )
        .collect(Collectors.toSet());

    Role role = new Role();

    role.setName("OWNER");
    role.setSystemRole(true);
    role.setActive(true);
    role.setCreatedAt(LocalDateTime.now());
    role.setPermissions(permissions);

    roleRepository.save(role);

    log.info(
        "Created OWNER role with {} permissions.",
        permissions.size()
    );
  }
}