package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.support.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("유저 조회 성공")
    void getUser_ShouldReturnUser() {
        // given
        User user = UserFixture.createUser();
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.getUser(user.getId());

        // then
        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getEmail(), response.getEmail());
    }

    @Test
    @DisplayName("유저 조회 실패 - 일치하는 유저 없음")
    void getUser_ShouldThrowException_WhenUserNotFound() {
        // given
        long userId = 1L;
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                userService.getUser(userId)
        );

        // then
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 유저 없음")
    void changePassword_ShouldThrowException_WhenUserNotFound() {
        // given
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPw", "newPw");

        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            userService.changePassword(userId, request);
        });

        // then
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호가 기존 비밀번호와 동일")
    void changePassword_ShouldThrowException_WhenNewPasswordEqualsOld() {
        // given
        long userId = 1L;
        User user = UserFixture.createUser();
        ReflectionTestUtils.setField(user, "id", 1L);

        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "newPassword");

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(true);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            userService.changePassword(userId, request);
        });

        // then
        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 기존 비밀번호가 틀림")
    void changePassword_ShouldThrowException_WhenOldPasswordWrong() {
        // given
        long userId = 1L;
        User user = UserFixture.createUser();
        ReflectionTestUtils.setField(user, "id", 1L);

        UserChangePasswordRequest request = new UserChangePasswordRequest("wrongOldPassword", "newPassword");

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(false);
        given(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).willReturn(false);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            userService.changePassword(userId, request);
        });

        // then
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_ShouldChangePassword() {
        // given
        long userId = 1L;
        User user = UserFixture.createUser();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "password", "encodedOldPassword");

        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "newPassword");

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(false);
        given(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).willReturn(true);
        given(passwordEncoder.encode(request.getNewPassword())).willReturn("encodedNewPassword");

        // when
        userService.changePassword(userId, request);

        // then (성공 시 핵심 필드만 검증)
        assertEquals("encodedNewPassword", user.getPassword());
    }
}