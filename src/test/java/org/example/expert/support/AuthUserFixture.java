package org.example.expert.support;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;

public class AuthUserFixture {
    private static final long DEFAULT_ID = 1L;
    private static final String DEFAULT_EMAIL = "testEmail";
    private static final UserRole DEFAULT_ROLE_ADMIN = UserRole.ADMIN;
    private static final UserRole DEFAULT_ROLE_USER = UserRole.USER;

    public static AuthUser createAdminAuthUser() {
        return new AuthUser(DEFAULT_ID,  DEFAULT_EMAIL, DEFAULT_ROLE_ADMIN);
    }

    public static AuthUser createAuthUser() {
        return new AuthUser(DEFAULT_ID,  DEFAULT_EMAIL, DEFAULT_ROLE_USER);
    }
}
