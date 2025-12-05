package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.support.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    @DisplayName("유저 Role 변경 성공")
    void changeUserRole_ShouldChangeUserRole() {
        // given
        long userId = 1L;
        User user = UserFixture.createUser();
        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // when
        userAdminService.changeUserRole(userId, request);

        // then
        assertEquals(UserRole.ADMIN, user.getUserRole());
    }

    @Test
    @DisplayName("유저 Role 변경 실패 - 존재하지 않는 유저")
    void changeUserRole_ShouldThrowException_WhenUserNotFound() {
        // given
        long userId = 1L;
        UserRoleChangeRequest request = new UserRoleChangeRequest(UserRole.ADMIN.name());

        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> userAdminService.changeUserRole(userId, request)
        );

        // then
        assertEquals("User not found", exception.getMessage());
    }
}
