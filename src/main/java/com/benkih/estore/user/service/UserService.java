package com.benkih.estore.user.service;

import com.benkih.estore.cart.dto.response.CartResponseDto;
import com.benkih.estore.cart.service.ICartService;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.order.dto.response.OrderResponseDto;
import com.benkih.estore.order.service.IOrderService;
import com.benkih.estore.product.service.IProductService;
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
//    log.info("User first reaching data={}", user.getCart().getItems());
    return convertToDto(user);
  }

  @Override
  public User createUser(CreateUserRequest request) {
    return Optional.of(request)
        .filter(user -> !userRepository.existsByEmail(request.getEmail()))
        .map(req -> {
          User user = new User();
          user.setEmail(request.getEmail());
          user.setPassword(passwordEncoder.encode(request.getPassword()));
          user.setFirstName(request.getFirstName());
          user.setLastName(request.getLastName());
          return userRepository.save(user);
        }).orElseThrow(() -> new AlreadyExistsException(request.getEmail() + " already exist!"));
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
    //    log.info("User cart data here={}", user.getCart().getItems());
    List<OrderResponseDto> orderDtos = Optional.ofNullable(user.getOrders())
        .orElse(Set.of())
        .stream()
        .map(order -> orderService.convertToDto(order))// Lambda form
    //        .map(orderService::convertToDto)
        .toList();

    //    CartResponseDto cartDto = Optional.ofNullable(user.getCart())
    //        .map(cartService::getConvertedCart)
    //        .orElse(null);
    CartResponseDto cartDto = null;

    if (user.getCart() != null) {
      cartDto = cartService.getConvertedCart(user.getCart());
    }
    log.info("User cart dto data={}", cartDto);
    return new UserResponseDto(
        user.getSlug(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        orderDtos,
        cartDto
    );
  }

  @Override
  public User getAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found."));
  }
}
