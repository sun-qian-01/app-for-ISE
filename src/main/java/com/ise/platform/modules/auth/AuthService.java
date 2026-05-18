package com.ise.platform.modules.auth;

import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.common.security.DataScope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final Map<String, DemoAccount> accounts;
    private final Map<String, CurrentUser> tokenStore = new ConcurrentHashMap<>();

    public AuthService() {
        this.accounts = Map.of(
            "20220001", new DemoAccount(
                1L, "20220001", "赵晨曦", "student", "123456", 1L,
                List.of("student"),
                List.of("student:profile:view", "student:dashboard:view", "notice:my:view", "kb:qa:ask", "application:create"),
                List.of(new DataScope("self", "1")),
                new AuthDto.StudentSummary(1L, "20220001", "赵晨曦", "2022", "软件工程", "软件工程2班")
            ),
            "20220018", new DemoAccount(
                2L, "20220018", "陈一诺", "student", "123456", 2L,
                List.of("class_cadre"),
                List.of("student:profile:view", "student:dashboard:view", "cadre:party:todo:view", "party:instance:scope:view", "party:todo:remind"),
                List.of(new DataScope("class", "软件工程2班"), new DataScope("branch", "本科生第一党支部")),
                new AuthDto.StudentSummary(2L, "20220018", "陈一诺", "2022", "软件工程", "软件工程2班")
            ),
            "teacher001", new DemoAccount(
                8L, "teacher001", "李老师", "teacher", "123456", null,
                List.of("teacher_admin"),
                List.of("admin:dashboard:view", "notice:publish", "application:approve", "application:reject", "audit:list:view"),
                List.of(new DataScope("department", "信息科学与工程学院")),
                null
            ),
            "leader001", new DemoAccount(
                18L, "leader001", "王院长", "leader", "123456", null,
                List.of("college_leader"),
                List.of("admin:dashboard:view", "audit:list:view", "system-log:list:view"),
                List.of(new DataScope("department", "信息科学与工程学院")),
                null
            )
        );
    }

    public AuthDto.LoginData login(String username, String password) {
        DemoAccount account = accounts.get(username);
        if (account == null || !account.password().equals(password)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "invalid username or password");
        }
        String token = "demo-token-" + username + "-" + UUID.randomUUID().toString().substring(0, 8);
        CurrentUser user = account.toCurrentUser();
        tokenStore.put(token, user);
        return new AuthDto.LoginData(token, toUserView(user, account.studentSummary()));
    }

    public AuthDto.UserView me(CurrentUser currentUser) {
        DemoAccount account = accounts.get(currentUser.getUsername());
        return toUserView(currentUser, account == null ? null : account.studentSummary());
    }

    public CurrentUser resolveCurrentUser(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        return tokenStore.get(token);
    }

    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            tokenStore.remove(token);
        }
    }

    private AuthDto.UserView toUserView(CurrentUser user, AuthDto.StudentSummary summary) {
        return new AuthDto.UserView(
            user.getId(),
            user.getUsername(),
            user.getRealName(),
            user.getUserType(),
            user.getRoles(),
            user.getPermissions(),
            user.getDataScopes(),
            summary
        );
    }

    private record DemoAccount(Long id,
                               String username,
                               String realName,
                               String userType,
                               String password,
                               Long studentId,
                               List<String> roles,
                               List<String> permissions,
                               List<DataScope> dataScopes,
                               AuthDto.StudentSummary studentSummary) {
        CurrentUser toCurrentUser() {
            return new CurrentUser(id, username, realName, userType, roles, permissions, dataScopes, studentId);
        }
    }
}
