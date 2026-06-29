package com.ise.platform.modules.auth;

import com.ise.platform.common.security.DataScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

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

    public static class RegisterStudentRequest {
        @NotBlank(message = "studentNo is required")
        @Pattern(regexp = "\\d{10}", message = "studentNo must be 10 digits")
        private String studentNo;

        @NotBlank(message = "name is required")
        private String name;

        @NotBlank(message = "grade is required")
        private String grade;

        @NotBlank(message = "major is required")
        private String major;

        @NotBlank(message = "className is required")
        private String className;

        private String phone;
        private String email;
        private String politicalStatusLabel;

        @NotBlank(message = "password is required")
        private String password;

        public String getStudentNo() {
            return studentNo;
        }

        public void setStudentNo(String studentNo) {
            this.studentNo = studentNo;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public String getMajor() {
            return major;
        }

        public void setMajor(String major) {
            this.major = major;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPoliticalStatusLabel() {
            return politicalStatusLabel;
        }

        public void setPoliticalStatusLabel(String politicalStatusLabel) {
            this.politicalStatusLabel = politicalStatusLabel;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RegisterStudentResponse {
        private String studentNo;
        private String name;

        public RegisterStudentResponse(String studentNo, String name) {
            this.studentNo = studentNo;
            this.name = name;
        }

        public String getStudentNo() {
            return studentNo;
        }

        public String getName() {
            return name;
        }
    }

    public static class ChangePasswordRequest {
        @NotBlank(message = "oldPassword is required")
        private String oldPassword;

        @NotBlank(message = "newPassword is required")
        private String newPassword;

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    public static class UserView {
        private Long id;
        private String username;
        private String realName;
        private String userType;
        private List<String> roles;
        private List<String> permissions;
        private List<MenuView> menus;
        private List<DataScope> dataScopes;
        private StudentSummary studentSummary;

        public UserView(Long id,
                        String username,
                        String realName,
                        String userType,
                        List<String> roles,
                        List<String> permissions,
                        List<MenuView> menus,
                        List<DataScope> dataScopes,
                        StudentSummary studentSummary) {
            this.id = id;
            this.username = username;
            this.realName = realName;
            this.userType = userType;
            this.roles = roles;
            this.permissions = permissions;
            this.menus = menus;
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

        public List<MenuView> getMenus() {
            return menus;
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

    public static class MenuView {
        private String code;
        private String name;
        private String path;

        public MenuView(String code, String name, String path) {
            this.code = code;
            this.name = name;
            this.path = path;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }
    }
}
