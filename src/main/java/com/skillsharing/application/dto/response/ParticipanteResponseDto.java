package com.skillsharing.application.dto.response;

import com.skillsharing.domain.entity.Inscripcion;
import lombok.Data;

import java.time.LocalDateTime;

// hu26: datos minimos para mostrar asistentes confirmados
@Data
public class ParticipanteResponseDto {

    private Long usuarioId;
    private String nombre;
    private String email;
    private String fotoPerfil;
    private String rolSesion;
    private LocalDateTime fechaInscripcion;

    public static ParticipanteResponseDto fromEntity(Inscripcion inscripcion) {
        ParticipanteResponseDto dto = new ParticipanteResponseDto();
        dto.setRolSesion(inscripcion.getRolSesion());
        dto.setFechaInscripcion(inscripcion.getFechaInscripcion());

        if (inscripcion.getUsuario() != null) {
            dto.setUsuarioId(inscripcion.getUsuario().getUsuarioId());
            dto.setNombre(inscripcion.getUsuario().getNombre());
            dto.setEmail(inscripcion.getUsuario().getEmail());
            dto.setFotoPerfil(inscripcion.getUsuario().getFotoPerfil());
        }

        return dto;
    }
}
