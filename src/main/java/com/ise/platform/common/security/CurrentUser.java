package com.ise.platform.common.security;

import java.util.List;

public class CurrentUser {

    private final Long id;
    private final String username;
    private final String realName;
    private final String userType;
    private final List<String> roles;
    private final List<String> permissions;
    private final List<DataScope> dataScopes;
    private final Long studentId;

    public CurrentUser(Long id,
                       String username,
                       String realName,
                       String userType,
                       List<String> roles,
                       List<String> permissions,
                       List<DataScope> dataScopes,
                       Long studentId) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.userType = userType;
        this.roles = roles;
        this.permissions = permissions;
        this.dataScopes = dataScopes;
        this.studentId = studentId;
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

    public Long getStudentId() {
        return studentId;
    }
}
