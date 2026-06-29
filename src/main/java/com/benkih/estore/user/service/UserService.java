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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
  private final UserRepository userRepository;
  private final IProductService productService;
  private final ICartService cartService;
  private final IOrderService orderService;

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
  public User createUser(CreateUserRequest request) {
    return Optional.of(request)
        .filter(user -> !userRepository.existsByEmail(request.getEmail()))
        .map(req -> {
          User user = new User();
          user.setEmail(request.getEmail());
          user.setPassword(request.getPassword());
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

    return new UserResponseDto(
        user.getSlug(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        orderDtos,
        cartDto
    );
  }
}
