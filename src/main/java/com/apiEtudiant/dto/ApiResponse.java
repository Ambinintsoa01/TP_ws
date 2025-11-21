package com.apiEtudiant.dto;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String status;
    private final T data;
    private final ErrorDetails error;
    private final Map<String, Object> meta;

    private ApiResponse(String status, T data, ErrorDetails error, Map<String, Object> meta) {
        this.status = status;
        this.data = data;
        this.error = error;
        this.meta = meta == null ? null : Collections.unmodifiableMap(meta);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, null);
    }

    public static <T> ApiResponse<T> success(T data, Map<String, Object> meta) {
        return new ApiResponse<>("success", data, null, meta);
    }

    public static <T> ApiResponse<T> error(ErrorDetails error) {
        return new ApiResponse<>("error", null, error, null);
    }

    public String getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public ErrorDetails getError() {
        return error;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }
}
