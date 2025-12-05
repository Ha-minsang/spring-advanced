package org.example.expert.support;

import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;

public class UserFixture {
    public static final String DEFAULT_EMAIL = "test@email.com";
    public static final String DEFAULT_PASSWORD = "testPassword1234";
    public static final UserRole DEFAULT_ROLE_ADMIN = UserRole.ADMIN;
    public static final UserRole DEFAULT_ROLE_USER = UserRole.USER;

    public static User createAdminUser() {
        return new User(DEFAULT_EMAIL, DEFAULT_PASSWORD, DEFAULT_ROLE_ADMIN);
    }

    public static User createUser() {
        return new User(DEFAULT_EMAIL, DEFAULT_PASSWORD, DEFAULT_ROLE_USER);
    }
}
