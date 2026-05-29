package com.skillsharing.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// clase generica de respuesta para todos los endpoints del api
// mantiene un formato uniforme en todas las respuestas (semana 7 - diseno de interfaces)
// principio ocp (semana 2): se puede extender con campos adicionales sin romper lo existente
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean ok;
    private String mensaje;
    private T data;

    // metodo de fabrica estatico para respuestas exitosas
    public static <T> ApiResponse<T> exito(String mensaje, T data) {
        return ApiResponse.<T>builder()
                .ok(true)
                .mensaje(mensaje)
                .data(data)
                .build();
    }

    // metodo de fabrica estatico para respuestas de error
    public static <T> ApiResponse<T> error(String mensaje) {
        return ApiResponse.<T>builder()
                .ok(false)
                .mensaje(mensaje)
                .build();
    }
}
