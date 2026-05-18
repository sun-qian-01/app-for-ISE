package com.ise.platform.common.api;

import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.web.RequestIdContext;

public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;
    private String requestId;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ErrorCode.SUCCESS.getCode());
        response.setMessage("ok");
        response.setData(data);
        response.setRequestId(RequestIdContext.get());
        return response;
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(errorCode.getCode());
        response.setMessage(message == null ? errorCode.getMessage() : message);
        response.setData(null);
        response.setRequestId(RequestIdContext.get());
        return response;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
