package com.benkih.estore.user.controller;


import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.user.dto.request.CreateUserRequest;
import com.benkih.estore.user.dto.request.UserUpdateRequest;
import com.benkih.estore.user.dto.response.UserResponseDto;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CONFLICT;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
  private final IUserService userService;

  //  @GetMapping("/user/{slug}")
  //  public ResponseEntity<ApiResponse> getUserBySlug(@PathVariable String slug){
  //    User user = userService.getUserBySlug(slug); // the global exception handler should capture the exceptions, so no need to wrap in try-and-catch
  //    UserResponseDto userDto = userService.convertToDto(user);
  //    return ResponseEntity.ok(new ApiResponse("success", "User data returned", userDto));
  //  }

  @GetMapping("/user/{slug}")
  public ResponseEntity<ApiResponse> getUserBySlug(@PathVariable String slug) {
    UserResponseDto dto = userService.getUserDtoBySlug(slug);
    return ResponseEntity.ok(new ApiResponse("success", "User data returned", dto));
  }

  @PostMapping("/user/add")
  public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody CreateUserRequest request){
    try {
      User user = userService.createUser(request);
      UserResponseDto userDto = userService.convertToDto(user);
      return ResponseEntity.ok(new ApiResponse("success", "User created", userDto ));
    } catch (AlreadyExistsException e) {
      return ResponseEntity.status(CONFLICT).body(new ApiResponse("fail", e.getMessage(), null));
    }
  }

  @PutMapping("/user/{slug}/update")
  public ResponseEntity<ApiResponse> updateUser(@RequestBody UserUpdateRequest request, @PathVariable String slug){
    User user = userService.updateUser(request, slug);
    UserResponseDto userDto = userService.convertToDto(user);
    return ResponseEntity.ok(new ApiResponse("success", "User updated", userDto ));
  }

  @DeleteMapping("/user/{slug}/delete")
  public ResponseEntity<ApiResponse> deleteUser(@PathVariable String slug){
    userService.deleteUser(slug);
    return ResponseEntity.ok(new ApiResponse("success", "User delete sucess", null));
  }

}
