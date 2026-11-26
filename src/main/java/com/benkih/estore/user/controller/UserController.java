package com.benkih.estore.user.controller;

import static org.springframework.http.HttpStatus.CONFLICT;

import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.security.user.StoreUserDetails;
import com.benkih.estore.user.dto.request.CreateUserRequest;
import com.benkih.estore.user.dto.request.UserUpdateRequest;
import com.benkih.estore.user.dto.response.UserResponseDto;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Users", description = "User profile and account administration")
@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    //  @GetMapping("/user/{slug}")
    //  public ResponseEntity<ApiResponse<UserResponseDto>> getUserBySlug(@PathVariable String
    // slug){
    //    User user = userService.getUserBySlug(slug); // the global exception handler should
    // capture the exceptions, so no need to wrap in try-and-catch
    //    UserResponseDto userDto = userService.convertToDto(user);
    //    return ResponseEntity.ok(new ApiResponse<>("success", "User data returned", userDto));
    //  }

    @GetMapping("/user/details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserBySlug(
            @AuthenticationPrincipal StoreUserDetails userDetails) {
        UserResponseDto dto = userService.getUserDtoBySlug(userDetails.getSlug());
        return ResponseEntity.ok(new ApiResponse<>("success", "User data returned", dto));
    }

    @GetMapping("/user/info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserInfo() {
        UserResponseDto dto = userService.getUserInfo();
        return ResponseEntity.ok(new ApiResponse<>("success", "User data returned", dto));
    }

    @PostMapping("/user/add")
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(request);
            UserResponseDto userDto = userService.convertToDto(user);
            return ResponseEntity.ok(new ApiResponse<>("success", "User created", userDto));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse<>("fail", e.getMessage(), null));
        }
    }

    @PutMapping("/user/{slug}/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @RequestBody UserUpdateRequest request, @PathVariable String slug) {
        User user = userService.updateUser(request, slug);
        UserResponseDto userDto = userService.convertToDto(user);
        return ResponseEntity.ok(new ApiResponse<>("success", "User updated", userDto));
    }

    @DeleteMapping("/user/{slug}/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String slug) {
        userService.deleteUser(slug);
        return ResponseEntity.ok(new ApiResponse<>("success", "User delete sucess", null));
    }
}
