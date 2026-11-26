package com.benkih.estore.user.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.benkih.estore.common.exceptions.ForbiddenException;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.user.dto.request.UserUpdateRequest;
import com.benkih.estore.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceAuthorizationTest {

    @Mock private CurrentUserService currentUserService;

    @Mock private UserRepository userRepository;

    @InjectMocks private UserServiceImpl userService;

    @Test
    void updateUserRejectsAnotherUsersSlug() {
        when(currentUserService.getCurrentUserSlug()).thenReturn("alice");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("Mallory");

        assertThrows(ForbiddenException.class, () -> userService.updateUser(request, "bob"));

        verify(userRepository, never()).findBySlug("bob");
    }

    @Test
    void deleteUserRejectsAnotherUsersSlug() {
        when(currentUserService.getCurrentUserSlug()).thenReturn("alice");

        assertThrows(ForbiddenException.class, () -> userService.deleteUser("bob"));

        verify(userRepository, never()).findBySlug("bob");
    }
}
