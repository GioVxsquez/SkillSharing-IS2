package com.skillsharing.application.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean ok;
    private String mensaje;
    private T data;
    public static <T> ApiResponse<T> exito(String mensaje, T data) {
        return ApiResponse.<T>builder()
                .ok(true)
                .mensaje(mensaje)
                .data(data)
                .build();
    }
    public static <T> ApiResponse<T> error(String mensaje) {
        return ApiResponse.<T>builder()
                .ok(false)
                .mensaje(mensaje)
                .build();
    }
}
