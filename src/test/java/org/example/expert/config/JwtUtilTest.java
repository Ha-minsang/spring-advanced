package org.example.expert.config;

import io.jsonwebtoken.Claims;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        String plainKey = "testSecretKeytestSecretKeytestSecretKeytestSecretKeytestSecretKey";
        String encodedKey = Base64.getEncoder().encodeToString(plainKey.getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtUtil, "secretKey", encodedKey);

        jwtUtil.init();
    }

    @Test
    @DisplayName("JWT 생성 성공")
    void createToken_ShouldCreateToken() {
        // given
        Long userId = 1L;
        String email = "test@email.com";
        UserRole userRole = UserRole.USER;

        // when
        String bearerToken = jwtUtil.createToken(userId, email, userRole);
        String token = jwtUtil.substringToken(bearerToken);
        Claims claims = jwtUtil.extractClaims(token);

        // then
        assertNotNull(bearerToken);
        assertTrue(bearerToken.startsWith("Bearer "));
        assertEquals(String.valueOf(userId), claims.getSubject());
        assertEquals(email, claims.get("email", String.class));
        assertEquals(userRole.name(), claims.get("userRole", String.class));
    }

    @Test
    @DisplayName("Bearer 토큰에서 실제 토큰 부분만 추출 성공")
    void substringToken_ShouldReturnToken() {
        // given
        String rawToken = "testRawTokentestRawTokentestRawTokentestRawToken";
        String bearerToken = "Bearer " + rawToken;

        // when
        String result = jwtUtil.substringToken(bearerToken);

        // then
        assertEquals(rawToken, result);
    }

    @Test
    @DisplayName("잘못된 형식의 토큰일 경우 ServerException 발생")
    void substringToken_ShouldThrowException_WhenTokenInvalid() {
        // given
        String invalidToken = "InvalidToken";

        // when
        ServerException exception = assertThrows(ServerException.class,
                () -> jwtUtil.substringToken(invalidToken));

        // then
        assertEquals("Not Found Token", exception.getMessage());
    }

    @Test
    @DisplayName("JWT에서 Claims 추출 성공")
    void extractClaims_ShouldExtractClaims() {
        // given
        Long userId = 2L;
        String email = "claims@test.com";
        UserRole userRole = UserRole.ADMIN;

        String bearerToken = jwtUtil.createToken(userId, email, userRole);
        String token = jwtUtil.substringToken(bearerToken);

        // when
        Claims claims = jwtUtil.extractClaims(token);

        // then
        assertEquals(String.valueOf(userId), claims.getSubject());
        assertEquals(email, claims.get("email", String.class));
        assertEquals(userRole.name(), claims.get("userRole", String.class));
    }
}
