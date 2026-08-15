package com.benkih.estore.user.service;

import com.benkih.estore.cart.dto.response.CartResponseDto;
import com.benkih.estore.cart.service.ICartService;
import com.benkih.estore.common.enums.RoleName;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.email.builder.WelcomeEmailBuilder;
import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.service.EmailService;
import com.benkih.estore.notification.NotificationService;
import com.benkih.estore.order.dto.response.OrderResponseDto;
import com.benkih.estore.order.service.IOrderService;
import com.benkih.estore.product.service.IProductService;
import com.benkih.estore.role.dto.response.RoleResponseDto;
import com.benkih.estore.role.service.RoleService;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.user.dto.request.CreateUserCommand;
import com.benkih.estore.user.dto.response.AddressResponseDto;
import com.benkih.estore.role.entity.Role;
import com.benkih.estore.role.repository.RoleRepository;
import com.benkih.estore.user.dto.request.CreateUserRequest;
import com.benkih.estore.user.dto.request.UserUpdateRequest;
import com.benkih.estore.user.dto.response.UserResponseDto;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
  private final UserRepository userRepository;
  private final IProductService productService;
  private final ICartService cartService;
  private final IOrderService orderService;
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;
  private final CurrentUserService currentUserService;
  private final AddressService addressService;
  private final RoleService roleService;
  private final NotificationService notificationService;


  @Override
  @Transactional(readOnly = true)
  public User getUserBySlug(String slug) {
    return userRepository.findBySlug(slug)
        .orElseThrow(()-> new ResourceNotFoundException("User not found"));
  }


  @Transactional(readOnly = true)
  @Override
  public UserResponseDto getUserDtoBySlug(String slug) {
    User user = userRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    return convertToDto(user);
  }


  @Override
  @Transactional
  public User createUser(CreateUserRequest request) {
    Set<Role> defaultRoles = new HashSet<>();

//    Role customerRole = roleRepository.findByName(RoleName.CUSTOMER.name())
//        .orElseThrow(() -> new RuntimeException("Default role not found: " + RoleName.CUSTOMER));
//
//    defaultRoles.add(customerRole);

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new AlreadyExistsException(request.getEmail() + " already exists!");
    }

    User user = new User();

    user.setEmail(request.getEmail());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setRoles(defaultRoles);

    user = userRepository.save(user);
//    log.info("User info detail={}", user);
    notificationService.sendWelcomeEmail(user);
    return user;
  }


  @Override
  public User updateUser(UserUpdateRequest request, String slug) {
    return userRepository.findBySlug(slug).map(existingUser -> {
      existingUser.setFirstName(request.getFirstName());
      existingUser.setLastName(request.getLastName());
      return userRepository.save(existingUser);
    }).orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }


  @Override
  public void deleteUser(String slug) {
    userRepository.findBySlug(slug).ifPresentOrElse(userRepository :: delete, () -> {
      throw new ResourceNotFoundException("User not found!");
    });
  }


  @Override
  public UserResponseDto convertToDto(User user){

    List<OrderResponseDto> orderDtos = Optional.ofNullable(user.getOrders())
        .orElse(Set.of())
        .stream()
        .map(order -> orderService.convertToDto(order))// Lambda form
        .toList();

    CartResponseDto cartDto = null;

    if (user.getCart() != null) {
      cartDto = cartService.getConvertedCart(user.getCart());
    }

    List<AddressResponseDto> addressDtos = Optional.ofNullable(user.getAddresses())
        .orElse(List.of())
        .stream()
        .map(addressService::convertToDto)
        .toList();

    List<RoleResponseDto> roleDtos = Optional.ofNullable(user.getRoles())
        .orElse(Set.of())
        .stream()
        .map(roleService::convertToDto)
        .toList();
//    log.info("User cart dto data={}", cartDto);

    return new UserResponseDto(
        user.getSlug(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        roleDtos,
        orderDtos,
        cartDto,
        addressDtos
    );
  }


  @Override
  public User getAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();

    return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found."));
  }


  @Override
  public UserResponseDto getUserInfo(){
    User user = currentUserService.getCurrentUser();

    return convertToDto(user);
  }


  public User createUserCommand(CreateUserCommand command) {
    User user = new User();

    user.setEmail(command.getEmail());
    user.setFirstName(command.getFirstName());
    user.setLastName(command.getLastName());
    user.setPhoneNumber(command.getPhoneNumber());
    if (command.getPassword() != null) {
      user.setPassword(passwordEncoder.encode(command.getPassword()));
    }
    user.setRoles(command.getRoles());
    user.setDepartments(command.getDepartments());
    user.setStatus(command.getStatus());
    user.setEmailVerified(command.isEmailVerified());

    return userRepository.save(user);
  }
}