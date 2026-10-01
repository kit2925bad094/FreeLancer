package com.freelancehub.dto;

import java.util.HashMap;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private HashMap<String, Object> metadata;

    public ApiResponse() {
        this.metadata = new HashMap<>();
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.metadata = new HashMap<>();
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.metadata = new HashMap<>();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public HashMap<String, Object> getMetadata() { return metadata; }
    public void setMetadata(HashMap<String, Object> metadata) { this.metadata = metadata; }
}
