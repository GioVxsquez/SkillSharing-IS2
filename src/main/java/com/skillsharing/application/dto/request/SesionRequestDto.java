package com.skillsharing.application.dto.request;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
// hu06: el instructor completa estos campos al crear una sesion
@Data
public class SesionRequestDto {
    @NotBlank(message = "el titulo es obligatorio")
    private String titulo;
    private String descripcion;
    @NotNull(message = "la fecha de la sesion es obligatoria")
    @Future(message = "la fecha debe ser futura")
    private LocalDateTime fechaSesion;
    @NotBlank(message = "la modalidad es obligatoria: VIRTUAL o PRESENCIAL")
    private String modalidad;
    private Integer maxParticipantes = 20;
    private String linkSesion;
    private String lugar;
    // hu28: habilidad requerida para la sesion (filtra en el buscador)
    private Long habilidadId;
}
