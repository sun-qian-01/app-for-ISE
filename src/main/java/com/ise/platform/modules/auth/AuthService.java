package com.ise.platform.modules.auth;

import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.common.security.DataScope;
import com.ise.platform.modules.student.StudentService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final String DEMO_PASSWORD_PREFIX = "{demo}";
    private static final String ACCOUNT_QUERY =
        """
            select u.id,
                   u.username,
                   u.password_hash,
                   u.real_name,
                   u.user_type,
                   u.role_code,
                   u.student_id,
                   u.status,
                   s.student_no,
                   s.name as student_name,
                   s.grade,
                   s.major,
                   s.class_name
              from sys_user u
              left join stu_student s
                on s.id = u.student_id and s.is_deleted = 0
             where u.is_deleted = 0
               and u.username = ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final StudentService studentService;
    private final Map<String, CurrentUser> tokenStore = new ConcurrentHashMap<>();

    public AuthService(JdbcTemplate jdbcTemplate, StudentService studentService) {
        this.jdbcTemplate = jdbcTemplate;
        this.studentService = studentService;
    }

    public AuthDto.LoginData login(String username, String password) {
        AccountRecord account = findAccount(username);
        if (account == null || !passwordMatches(password, account.passwordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "invalid username or password");
        }
        if (!"enabled".equalsIgnoreCase(account.status())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "account is disabled");
        }

        String roleCode = resolveRoleCode(account);
        CurrentUser user = toCurrentUser(account, roleCode);

        String token = "demo-token-" + username + "-" + UUID.randomUUID().toString().substring(0, 8);
        tokenStore.put(token, user);
        return new AuthDto.LoginData(token, toUserView(user, account.toStudentSummary()));
    }

    public AuthDto.UserView me(CurrentUser currentUser) {
        AccountRecord account = findAccount(currentUser.getUsername());
        if (account == null) {
            return toUserView(currentUser, null);
        }

        String roleCode = resolveRoleCode(account);
        CurrentUser refreshedUser = toCurrentUser(account, roleCode);
        return toUserView(refreshedUser, account.toStudentSummary());
    }

    @Transactional
    public AuthDto.RegisterStudentResponse registerStudent(AuthDto.RegisterStudentRequest request) {
        String studentNo = trim(request.getStudentNo());
        if (!studentNo.matches("\\d{10}")) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "studentNo must be 10 digits");
        }
        if (findAccount(studentNo) != null) {
            throw new BusinessException(ErrorCode.STATUS_CONFLICT, "student account already exists");
        }
        Integer existingStudent = jdbcTemplate.queryForObject(
            "select count(*) from stu_student where student_no = ? and is_deleted = 0",
            Integer.class,
            studentNo
        );
        if (existingStudent != null && existingStudent > 0) {
            throw new BusinessException(ErrorCode.STATUS_CONFLICT, "student already exists");
        }

        Long studentId = nextId("stu_student");
        Long userId = nextId("sys_user");
        String politicalStatus = StringUtils.hasText(request.getPoliticalStatusLabel())
            ? request.getPoliticalStatusLabel().trim()
            : "群众";

        jdbcTemplate.update(
            """
                insert into stu_student (
                    id, student_no, name, phone, email, grade, major, class_name,
                    political_status, status, created_at, updated_at, is_deleted
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, 'active', current_timestamp, current_timestamp, 0)
                """,
            studentId,
            studentNo,
            trim(request.getName()),
            trim(request.getPhone()),
            trim(request.getEmail()),
            trim(request.getGrade()),
            trim(request.getMajor()),
            trim(request.getClassName()),
            politicalStatus
        );
        jdbcTemplate.update(
            """
                insert into sys_user (
                    id, username, password_hash, real_name, user_type, role_code,
                    student_id, status, created_at, updated_at, is_deleted
                ) values (?, ?, ?, ?, 'student', 'student', ?, 'enabled', current_timestamp, current_timestamp, 0)
                """,
            userId,
            studentNo,
            encodePassword(request.getPassword()),
            trim(request.getName()),
            studentId
        );
        studentService.addRegisteredStudent(
            studentId,
            studentNo,
            trim(request.getName()),
            trim(request.getGrade()),
            trim(request.getMajor()),
            trim(request.getClassName()),
            politicalStatus,
            trim(request.getPhone()),
            trim(request.getEmail())
        );
        return new AuthDto.RegisterStudentResponse(studentNo, trim(request.getName()));
    }

    public void changePassword(CurrentUser currentUser, String oldPassword, String newPassword) {
        AccountRecord account = findAccount(currentUser.getUsername());
        if (account == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "login required");
        }
        if (!passwordMatches(oldPassword, account.passwordHash())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "old password is incorrect");
        }
        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "new password is required");
        }
        if (Objects.equals(oldPassword, newPassword)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "new password must be different");
        }

        jdbcTemplate.update(
            "update sys_user set password_hash = ?, updated_at = current_timestamp where id = ? and is_deleted = 0",
            encodePassword(newPassword),
            account.id()
        );
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

    private AccountRecord findAccount(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        List<AccountRecord> rows = jdbcTemplate.query(
            ACCOUNT_QUERY,
            (rs, rowNum) -> new AccountRecord(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("real_name"),
                rs.getString("user_type"),
                rs.getString("role_code"),
                rs.getObject("student_id", Long.class),
                rs.getString("status"),
                rs.getString("student_no"),
                rs.getString("student_name"),
                rs.getString("grade"),
                rs.getString("major"),
                rs.getString("class_name")
            ),
            username.trim()
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    private boolean passwordMatches(String rawPassword, String storedPasswordHash) {
        if (!StringUtils.hasText(storedPasswordHash) || rawPassword == null) {
            return false;
        }
        if (storedPasswordHash.startsWith(DEMO_PASSWORD_PREFIX)) {
            return Objects.equals(storedPasswordHash.substring(DEMO_PASSWORD_PREFIX.length()), rawPassword);
        }
        return Objects.equals(storedPasswordHash, rawPassword);
    }

    private String encodePassword(String rawPassword) {
        return DEMO_PASSWORD_PREFIX + rawPassword;
    }

    private Long nextId(String tableName) {
        Long value = jdbcTemplate.queryForObject("select coalesce(max(id), 0) + 1 from " + tableName, Long.class);
        return value == null ? 1L : value;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String resolveRoleCode(AccountRecord account) {
        if (StringUtils.hasText(account.roleCode())) {
            return account.roleCode().trim();
        }
        return switch (account.userType().toLowerCase(Locale.ROOT)) {
            case "teacher" -> "teacher_admin";
            case "leader" -> "college_leader";
            default -> "student";
        };
    }

    private CurrentUser toCurrentUser(AccountRecord account, String roleCode) {
        List<String> roles = List.of(roleCode);
        return new CurrentUser(
            account.id(),
            account.username(),
            account.realName(),
            account.userType(),
            roles,
            permissionsByRole(roleCode),
            dataScopesByRole(account, roleCode),
            account.studentId()
        );
    }

    private AuthDto.UserView toUserView(CurrentUser user, AuthDto.StudentSummary summary) {
        return new AuthDto.UserView(
            user.getId(),
            user.getUsername(),
            user.getRealName(),
            user.getUserType(),
            user.getRoles(),
            user.getPermissions(),
            buildMenus(user.getRoles()),
            user.getDataScopes(),
            summary
        );
    }

    private List<String> permissionsByRole(String roleCode) {
        return switch (roleCode) {
            case "teacher_admin" -> List.of(
                "admin:dashboard:view",
                "notice:publish",
                "application:approve",
                "application:reject",
                "audit:list:view",
                "student:sensitive:view",
                "file:upload"
            );
            case "college_leader" -> List.of(
                "admin:dashboard:view",
                "audit:list:view",
                "system-log:list:view",
                "file:upload"
            );
            case "system_admin" -> List.of(
                "admin:dashboard:view",
                "notice:publish",
                "application:approve",
                "application:reject",
                "audit:list:view",
                "system-log:list:view",
                "student:sensitive:view",
                "file:upload"
            );
            case "class_cadre" -> List.of(
                "student:profile:view",
                "student:dashboard:view",
                "student:honor:view",
                "notice:my:view",
                "kb:qa:ask",
                "application:create",
                "file:upload",
                "cadre:party:todo:view",
                "party:instance:scope:view",
                "party:todo:remind"
            );
            default -> List.of(
                "student:profile:view",
                "student:dashboard:view",
                "notice:my:view",
                "kb:qa:ask",
                "application:create",
                "file:upload"
            );
        };
    }

    private List<DataScope> dataScopesByRole(AccountRecord account, String roleCode) {
        List<DataScope> scopes = new ArrayList<>();
        if ("class_cadre".equals(roleCode)) {
            if (StringUtils.hasText(account.className())) {
                scopes.add(new DataScope("class", account.className()));
            }
            scopes.add(new DataScope("branch", "本科生第一党支部"));
            return scopes;
        }
        if ("student".equals(roleCode) && account.studentId() != null) {
            scopes.add(new DataScope("self", String.valueOf(account.studentId())));
            return scopes;
        }
        if ("teacher_admin".equals(roleCode)) {
            scopes.add(new DataScope("class", "软件工程2班"));
            return scopes;
        }
        if ("college_leader".equals(roleCode)) {
            scopes.add(new DataScope("department", "信息科学与工程学院"));
            return scopes;
        }
        scopes.add(new DataScope("all", "*"));
        return scopes;
    }

    private List<AuthDto.MenuView> buildMenus(List<String> roles) {
        if (roles.contains("teacher_admin") || roles.contains("system_admin")) {
            return List.of(
                new AuthDto.MenuView("admin_dashboard", "管理首页", "/admin/dashboard"),
                new AuthDto.MenuView("admin_students", "学生画像", "/admin/students"),
                new AuthDto.MenuView("admin_notices", "精准通知", "/admin/notices"),
                new AuthDto.MenuView("admin_applications", "审批处理", "/admin/applications")
            );
        }
        if (roles.contains("college_leader")) {
            return List.of(
                new AuthDto.MenuView("leader_dashboard", "领导看板", "/leader/dashboard")
            );
        }
        return List.of(
            new AuthDto.MenuView("student_dashboard", "学生首页", "/student/dashboard"),
            new AuthDto.MenuView("student_notices", "通知中心", "/student/notices"),
            new AuthDto.MenuView("student_profile", "个人画像", "/student/profile")
        );
    }

    private record AccountRecord(Long id,
                                 String username,
                                 String passwordHash,
                                 String realName,
                                 String userType,
                                 String roleCode,
                                 Long studentId,
                                 String status,
                                 String studentNo,
                                 String studentName,
                                 String grade,
                                 String major,
                                 String className) {
        AuthDto.StudentSummary toStudentSummary() {
            if (studentId == null) {
                return null;
            }
            return new AuthDto.StudentSummary(
                studentId,
                studentNo,
                studentName,
                grade,
                major,
                className
            );
        }
    }
}
