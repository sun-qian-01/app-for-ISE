package com.ise.platform.common.error;

public enum ErrorCode {
    SUCCESS(0, "ok"),
    PARAM_INVALID(40001, "parameter validation failed"),
    UNAUTHORIZED(40100, "unauthorized"),
    FORBIDDEN(40300, "forbidden"),
    NOT_FOUND(40400, "resource not found"),
    STATUS_CONFLICT(40900, "status conflict"),
    FILE_TOO_LARGE(41300, "file too large"),
    INTERNAL_ERROR(50000, "internal error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
