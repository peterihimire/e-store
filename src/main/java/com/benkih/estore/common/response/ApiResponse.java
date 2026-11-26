package com.benkih.estore.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>("success", message, data);
    }

    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>("fail", message, null);
    }
}
