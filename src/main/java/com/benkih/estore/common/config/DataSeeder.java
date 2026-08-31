package com.benkih.estore.common.config;

import com.benkih.estore.common.enums.AttributeType;
import com.benkih.estore.common.enums.UserStatus;
import com.benkih.estore.permission.entity.Permission;
import com.benkih.estore.permission.repository.PermissionRepository;
import com.benkih.estore.product.entity.AttributeValue;
import com.benkih.estore.product.entity.Category;
import com.benkih.estore.product.entity.CategoryAttribute;
import com.benkih.estore.product.repository.AttributeValueRepository;
import com.benkih.estore.product.repository.CategoryAttributeRepository;
import com.benkih.estore.product.repository.CategoryRepository;
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

  private final CategoryRepository categoryRepository;
  private final CategoryAttributeRepository categoryAttributeRepository;
  private final AttributeValueRepository attributeValueRepository;


  @Override
  public void run(String... args) {
    try {
      log.info("Starting data seeding...");
//      seedPermissions();
//      seedSystemRoles();
//      createSuperAdmin();
//      seedMarketplaceAttributes();

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

  private void seedMarketplaceAttributes() {

    log.info("Seeding marketplace categories and attributes...");

    // ============================================================
    // CLOTHING
    // ============================================================

    Category clothing = getOrCreateCategory("Clothing");

    CategoryAttribute clothingColor =
        getOrCreateAttribute(clothing, "Color", AttributeType.SELECT, true, true, 1);

    CategoryAttribute clothingSize =
        getOrCreateAttribute(clothing, "Size", AttributeType.SELECT, true, true, 2);

    CategoryAttribute clothingMaterial =
        getOrCreateAttribute(clothing, "Material",AttributeType.SELECT, false, false, 3);

    CategoryAttribute clothingGender =
        getOrCreateAttribute(clothing, "Gender", AttributeType.SELECT, true, false, 4);

    seedAttributeValues(
        clothingColor,
        "Black",
        "White",
        "Red",
        "Blue",
        "Green",
        "Yellow",
        "Grey",
        "Brown",
        "Orange",
        "Purple",
        "Pink"
    );

    seedAttributeValues(
        clothingSize,
        "XS",
        "S",
        "M",
        "L",
        "XL",
        "XXL",
        "XXXL"
    );

    seedAttributeValues(
        clothingMaterial,
        "Cotton",
        "Polyester",
        "Wool",
        "Linen",
        "Denim",
        "Leather",
        "Silk",
        "Nylon",
        "Rayon",
        "Viscose"
    );

    seedAttributeValues(
        clothingGender,
        "Men",
        "Women",
        "Unisex",
        "Boys",
        "Girls"
    );


    // ============================================================
    // SHOES
    // ============================================================

    Category shoes = getOrCreateCategory("Shoes");

    CategoryAttribute shoesColor = getOrCreateAttribute(
        shoes, "Color", AttributeType.SELECT, true, true, 1
    );

    CategoryAttribute shoesSize = getOrCreateAttribute(
        shoes, "Size", AttributeType.SELECT, true, true, 2
    );

    CategoryAttribute shoesMaterial = getOrCreateAttribute(
        shoes, "Material", AttributeType.SELECT, false, false, 3
    );

    CategoryAttribute shoesGender = getOrCreateAttribute(
        shoes, "Gender", AttributeType.SELECT, true, false, 4
    );

    seedAttributeValues(
        shoesColor,
        "Black",
        "White",
        "Red",
        "Blue",
        "Green",
        "Brown",
        "Grey",
        "Yellow",
        "Orange",
        "Purple"
    );

    seedAttributeValues(
        shoesSize,
        "36",
        "37",
        "38",
        "39",
        "40",
        "41",
        "42",
        "43",
        "44",
        "45",
        "46",
        "47",
        "48"
    );

    seedAttributeValues(
        shoesMaterial,
        "Leather",
        "Suede",
        "Canvas",
        "Mesh",
        "Synthetic",
        "Rubber",
        "Textile"
    );

    seedAttributeValues(
        shoesGender,
        "Men",
        "Women",
        "Unisex",
        "Boys",
        "Girls"
    );


    // ============================================================
    // SMARTPHONES
    // ============================================================

    Category smartphones =
        getOrCreateCategory("Smartphones");

    CategoryAttribute smartphoneColor = getOrCreateAttribute(
        smartphones, "Color", AttributeType.SELECT, true, true, 1
    );

    CategoryAttribute smartphoneStorage = getOrCreateAttribute(
        smartphones, "Storage", AttributeType.SELECT, true, true, 2
    );

    CategoryAttribute smartphoneRam = getOrCreateAttribute(
        smartphones, "RAM", AttributeType.SELECT, false, true, 3
    );

    CategoryAttribute smartphoneScreenSize = getOrCreateAttribute(
        smartphones, "Screen Size", AttributeType.SELECT, true, false, 4
    );

    seedAttributeValues(
        smartphoneColor,
        "Black",
        "White",
        "Blue",
        "Red",
        "Green",
        "Gold",
        "Silver",
        "Purple",
        "Grey"
    );

    seedAttributeValues(
        smartphoneStorage,
        "32GB",
        "64GB",
        "128GB",
        "256GB",
        "512GB",
        "1TB"
    );

    seedAttributeValues(
        smartphoneRam,
        "2GB",
        "3GB",
        "4GB",
        "6GB",
        "8GB",
        "12GB",
        "16GB",
        "24GB"
    );

    seedAttributeValues(
        smartphoneScreenSize,
        "5.0 inches",
        "5.5 inches",
        "6.0 inches",
        "6.1 inches",
        "6.3 inches",
        "6.5 inches",
        "6.7 inches",
        "6.9 inches"
    );


    // ============================================================
    // LAPTOPS
    // ============================================================

    Category laptops =
        getOrCreateCategory("Laptops");

    CategoryAttribute laptopColor = getOrCreateAttribute(
        laptops, "Color", AttributeType.SELECT, false, true, 1
    );

    CategoryAttribute laptopRam = getOrCreateAttribute(
        laptops, "RAM", AttributeType.SELECT, true, true, 2
    );

    CategoryAttribute laptopStorage = getOrCreateAttribute(
        laptops, "Storage", AttributeType.SELECT, true, true, 3
    );

    CategoryAttribute laptopProcessor = getOrCreateAttribute(
        laptops, "Processor", AttributeType.SELECT, true, true, 4
    );

    CategoryAttribute laptopScreenSize = getOrCreateAttribute(
        laptops, "Screen Size", AttributeType.SELECT, true, false, 5
    );

    seedAttributeValues(
        laptopColor,
        "Black",
        "Silver",
        "Grey",
        "White",
        "Blue"
    );

    seedAttributeValues(
        laptopRam,
        "4GB",
        "8GB",
        "16GB",
        "32GB",
        "64GB"
    );

    seedAttributeValues(
        laptopStorage,
        "128GB SSD",
        "256GB SSD",
        "512GB SSD",
        "1TB SSD",
        "2TB SSD",
        "4TB SSD"
    );

    seedAttributeValues(
        laptopProcessor,
        "Intel Core i3",
        "Intel Core i5",
        "Intel Core i7",
        "Intel Core i9",
        "AMD Ryzen 3",
        "AMD Ryzen 5",
        "AMD Ryzen 7",
        "AMD Ryzen 9",
        "Apple M1",
        "Apple M2",
        "Apple M3",
        "Apple M4",
        "Apple M5"
    );

    seedAttributeValues(
        laptopScreenSize,
        "11.6 inches",
        "13.3 inches",
        "14 inches",
        "15.6 inches",
        "16 inches",
        "17.3 inches"
    );


    // ============================================================
    // CARS
    // ============================================================

    Category cars =
        getOrCreateCategory("Cars");

    CategoryAttribute carMake =
        getOrCreateAttribute(cars, "Make", AttributeType.SELECT, true, false, 1);

    CategoryAttribute carModel =
        getOrCreateAttribute(cars, "Model", AttributeType.TEXT, true, false, 2);

    CategoryAttribute carYear =
        getOrCreateAttribute(cars, "Year", AttributeType.NUMBER, true, false, 3);

    CategoryAttribute carColor = getOrCreateAttribute(
        cars, "Color", AttributeType.SELECT, true, false, 4
    );

    CategoryAttribute carTransmission = getOrCreateAttribute(
        cars, "Transmission", AttributeType.SELECT, true, false, 5
    );

    CategoryAttribute carFuelType = getOrCreateAttribute(
        cars, "Fuel Type", AttributeType.SELECT, true, false, 6
    );

    CategoryAttribute carBodyType = getOrCreateAttribute(
        cars, "Body Type", AttributeType.SELECT, true, false, 7
    );

    seedAttributeValues(
        carMake,
        "Toyota",
        "Honda",
        "Mercedes-Benz",
        "BMW",
        "Audi",
        "Lexus",
        "Ford",
        "Hyundai",
        "Kia",
        "Nissan",
        "Volkswagen",
        "Mazda",
        "Volvo",
        "Peugeot",
        "Land Rover",
        "Porsche",
        "Jeep"
    );

    /*
     * Do not seed car models globally.
     *
     * Models depend on the selected manufacturer and are better
     * represented as product data.
     *
     * Example:
     *
     * Toyota → Camry
     * Toyota → Corolla
     * Toyota → Highlander
     * Honda  → Accord
     * Honda  → Civic
     *
     * These belong to actual vehicle listings/products.
     */

    seedAttributeValues(
        carYear,
        "2020",
        "2021",
        "2022",
        "2023",
        "2024",
        "2025",
        "2026"
    );

    seedAttributeValues(
        carColor,
        "Black",
        "White",
        "Silver",
        "Grey",
        "Red",
        "Blue",
        "Brown",
        "Green",
        "Gold"
    );

    seedAttributeValues(
        carTransmission,
        "Automatic",
        "Manual",
        "CVT",
        "DCT"
    );

    seedAttributeValues(
        carFuelType,
        "Petrol",
        "Diesel",
        "Hybrid",
        "Electric",
        "Plug-in Hybrid"
    );

    seedAttributeValues(
        carBodyType,
        "Sedan",
        "SUV",
        "Hatchback",
        "Coupe",
        "Convertible",
        "Wagon",
        "Pickup",
        "Van",
        "Minivan"
    );


    // ============================================================
    // PROPERTIES
    // ============================================================

    Category properties =
        getOrCreateCategory("Properties");

    CategoryAttribute propertyType = getOrCreateAttribute(
        properties, "Property Type", AttributeType.SELECT, true, false, 1
    );

    CategoryAttribute bedrooms = getOrCreateAttribute(
        properties, "Bedrooms", AttributeType.SELECT, false, false, 2
    );

    CategoryAttribute bathrooms = getOrCreateAttribute(
        properties, "Bathrooms", AttributeType.SELECT, false, false, 3
    );

    CategoryAttribute furnishing = getOrCreateAttribute(
        properties, "Furnishing", AttributeType.SELECT, false, false, 4
    );

    CategoryAttribute parking = getOrCreateAttribute(
        properties, "Parking", AttributeType.SELECT, false, false, 5
    );

    seedAttributeValues(
        propertyType,
        "Apartment",
        "House",
        "Duplex",
        "Terrace",
        "Bungalow",
        "Penthouse",
        "Villa",
        "Townhouse",
        "Land",
        "Office",
        "Shop",
        "Warehouse"
    );

    seedAttributeValues(
        bedrooms,
        "Studio",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6+"
    );

    seedAttributeValues(
        bathrooms,
        "1",
        "2",
        "3",
        "4",
        "5+"
    );

    seedAttributeValues(
        furnishing,
        "Unfurnished",
        "Semi-Furnished",
        "Furnished"
    );

    seedAttributeValues(
        parking,
        "None",
        "1",
        "2",
        "3",
        "4+"
    );

    log.info(
        "Marketplace categories and attributes seeded successfully."
    );
  }

  private Category getOrCreateCategory(String name) {

    return categoryRepository
        .findByNameAndParentIsNull(name)
        .orElseGet(() -> {

          Category category = new Category(name);

          category.setActive(true);
          category.setDisplayOrder(0);

          return categoryRepository.save(category);
        });
  }

  private Category getOrCreateCategory(
      String name,
      Category parent
  ) {

    return categoryRepository
        .findByNameAndParent(name, parent)
        .orElseGet(() -> {

          Category category = new Category(name);

          category.setParent(parent);
          category.setActive(true);
          category.setDisplayOrder(0);

          return categoryRepository.save(category);
        });
  }

  private CategoryAttribute getOrCreateAttribute(
      Category category,
      String name,
      AttributeType type,
      boolean required,
      boolean variantAttribute,
      int displayOrder
  ) {
    CategoryAttribute attribute = categoryAttributeRepository
        .findByCategoryAndName(category, name)
        .orElseGet(CategoryAttribute::new);

    attribute.setCategory(category);
    attribute.setName(name);
    attribute.setType(type);
    attribute.setRequired(required);
    attribute.setVariantAttribute(variantAttribute);
    attribute.setActive(true);
    attribute.setDisplayOrder(displayOrder);

    return categoryAttributeRepository.save(attribute);
  }

  private void seedAttributeValues(
      CategoryAttribute attribute,
      String... values
  ) {
    for (int index = 0; index < values.length; index++) {
      String value = values[index];

      AttributeValue attributeValue = attributeValueRepository
          .findByAttributeAndValue(attribute, value)
          .orElseGet(AttributeValue::new);

      attributeValue.setAttribute(attribute);
      attributeValue.setValue(value);
      attributeValue.setDisplayName(value);
      attributeValue.setActive(true);
      attributeValue.setDisplayOrder(index + 1);

      attributeValueRepository.save(attributeValue);

      log.info(
          "Seeded attribute value: {} → {}",
          attribute.getName(),
          value
      );
    }
  }
}