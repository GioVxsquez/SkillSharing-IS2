package com.skillsharing.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// dto para la carga de metadatos de un material educativo (hu05)
// el archivo fisico se recibe aparte (multipartfile)
@Data
public class MaterialRequestDto {

    @NotBlank(message = "el nombre del material es obligatorio")
    private String nombre;

    private String tipoArchivo;
}
