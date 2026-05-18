package com.ise.platform.common.security;

public class DataScope {

    private final String scopeType;
    private final String scopeValue;

    public DataScope(String scopeType, String scopeValue) {
        this.scopeType = scopeType;
        this.scopeValue = scopeValue;
    }

    public String getScopeType() {
        return scopeType;
    }

    public String getScopeValue() {
        return scopeValue;
    }
}
