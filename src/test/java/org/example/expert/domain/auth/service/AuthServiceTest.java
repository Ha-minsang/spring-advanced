package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    void signup_ShouldThrowException_WhenEmailAlreadyExists() {
        // given
        String email = "test@email.com";
        SignupRequest request = new SignupRequest(email, "testPassword1234", "USER");

        given(userRepository.existsByEmail(email)).willReturn(true);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                authService.signup(request));

        // then
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_ShouldSignup() {
        // given
        String email = "test@email.com";
        String rawPassword = "testPassword1234";
        String encodedPassword = "encodedPassword";
        String userRoleName = "USER";
        UserRole userRole = UserRole.USER;

        SignupRequest request = new SignupRequest(email, rawPassword, userRoleName);

        User savedUser = new User(email, encodedPassword, userRole);
        ReflectionTestUtils.setField(savedUser, "id", 1L);

        String bearerToken = "testBearerToken";

        given(userRepository.existsByEmail(email)).willReturn(false);
        given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUserRole()))
                .willReturn(bearerToken);

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertNotNull(response);
    }

    @Test
    @DisplayName("로그인 실패 - 가입되지 않은 유저")
    void signin_ShouldThrowException_WhenUserNotFound() {
        // given
        String email = "notfound@email.com";
        SigninRequest request = new SigninRequest(email, "testPassword1234");

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> authService.signin(request));

        // then
        assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void signin_ShouldThrowException_WhenPasswordIncorrect() {
        // given
        String email = UserFixture.DEFAULT_EMAIL;
        String rawPassword = "wrongPassword";
        User user = UserFixture.createUser();
        ReflectionTestUtils.setField(user, "id", 1L);

        SigninRequest request = new SigninRequest(email, rawPassword);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(rawPassword, user.getPassword())).willReturn(false);

        // when
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.signin(request));

        // then
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("로그인 성공")
    void signin_ShouldSignin() {
        // given
        String email = UserFixture.DEFAULT_EMAIL;
        String rawPassword = UserFixture.DEFAULT_PASSWORD;
        String encodedPassword = "encodedPassword";
        String token = "Bearer token";

        User user = new User(email, encodedPassword, UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        SigninRequest request = new SigninRequest(email, rawPassword);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(rawPassword, user.getPassword())).willReturn(true);
        given(jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole()))
                .willReturn(token);

        // when
        SigninResponse response = authService.signin(request);

        // then
        assertNotNull(response);
        assertEquals(token, response.getBearerToken());
    }
}
