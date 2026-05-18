package com.ise.platform.common.security;

import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;

import java.util.Arrays;

public final class AuthContext {

    private static final ThreadLocal<CurrentUser> USER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(CurrentUser user) {
        USER.set(user);
    }

    public static CurrentUser get() {
        return USER.get();
    }

    public static CurrentUser requireUser() {
        CurrentUser user = USER.get();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "login required");
        }
        return user;
    }

    public static void requireAnyRole(String... roles) {
        CurrentUser user = requireUser();
        boolean matched = user.getRoles().stream().anyMatch(role -> Arrays.asList(roles).contains(role));
        if (!matched) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "permission denied");
        }
    }

    public static void clear() {
        USER.remove();
    }
}
