package com.skillsharing.application.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

// dto para crear o editar una sesion de aprendizaje
// hu06: el instructor completa estos campos al crear una sesion
@Data
public class SesionRequestDto {

    @NotBlank(message = "el titulo es obligatorio")
    private String titulo;

    private String descripcion;

    @NotNull(message = "la fecha de la sesion es obligatoria")
    @Future(message = "la fecha debe ser futura")
    private LocalDateTime fechaSesion;

    // VIRTUAL o PRESENCIAL - el factory usara esto para decidir que crear (semana 3)
    @NotBlank(message = "la modalidad es obligatoria: VIRTUAL o PRESENCIAL")
    private String modalidad;

    private Integer maxParticipantes = 20;

    // solo para sesiones virtuales
    private String linkSesion;

    // solo para sesiones presenciales
    private String lugar;

    // hu28: habilidad requerida para la sesion (filtra en el buscador)
    private Long habilidadId;
}
