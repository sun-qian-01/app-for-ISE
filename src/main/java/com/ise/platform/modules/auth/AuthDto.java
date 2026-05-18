package com.ise.platform.modules.auth;

import com.ise.platform.common.security.DataScope;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public final class AuthDto {

    private AuthDto() {
    }

    public static class LoginRequest {
        @NotBlank(message = "username is required")
        private String username;

        @NotBlank(message = "password is required")
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class LoginData {
        private String token;
        private UserView user;

        public LoginData(String token, UserView user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        public UserView getUser() {
            return user;
        }
    }

    public static class UserView {
        private Long id;
        private String username;
        private String realName;
        private String userType;
        private List<String> roles;
        private List<String> permissions;
        private List<DataScope> dataScopes;
        private StudentSummary studentSummary;

        public UserView(Long id,
                        String username,
                        String realName,
                        String userType,
                        List<String> roles,
                        List<String> permissions,
                        List<DataScope> dataScopes,
                        StudentSummary studentSummary) {
            this.id = id;
            this.username = username;
            this.realName = realName;
            this.userType = userType;
            this.roles = roles;
            this.permissions = permissions;
            this.dataScopes = dataScopes;
            this.studentSummary = studentSummary;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getRealName() {
            return realName;
        }

        public String getUserType() {
            return userType;
        }

        public List<String> getRoles() {
            return roles;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public List<DataScope> getDataScopes() {
            return dataScopes;
        }

        public StudentSummary getStudentSummary() {
            return studentSummary;
        }
    }

    public static class StudentSummary {
        private Long studentId;
        private String studentNo;
        private String name;
        private String grade;
        private String major;
        private String className;

        public StudentSummary(Long studentId, String studentNo, String name, String grade, String major, String className) {
            this.studentId = studentId;
            this.studentNo = studentNo;
            this.name = name;
            this.grade = grade;
            this.major = major;
            this.className = className;
        }

        public Long getStudentId() {
            return studentId;
        }

        public String getStudentNo() {
            return studentNo;
        }

        public String getName() {
            return name;
        }

        public String getGrade() {
            return grade;
        }

        public String getMajor() {
            return major;
        }

        public String getClassName() {
            return className;
        }
    }
}
