package com.skillsharing.application.dto.response;

import com.skillsharing.domain.entity.SesionAprendizaje;
import lombok.Data;
import java.time.LocalDateTime;

// dto de respuesta para sesiones - el controller nunca expone la entidad jpa directamente
// principio dip (semana 2): la capa web depende del dto, no de la entidad de dominio
// poo (semana 1): encapsulamiento - el cliente solo ve lo que necesita ver
@Data
public class SesionResponseDto {

    private Long sesionId;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaSesion;
    private String modalidad;
    private String tipo;
    private String estado;
    private Integer maxParticipantes;
    private Integer capacidadMaxima;
    private Integer duracionMinutos;
    private String linkSesion;
    private String lugar;
    private String ubicacion;
    private Long instructorId;
    private String instructorNombre;
    private LocalDateTime fechaCreacion;
    // us09: categoria para filtros del home
    private String categoria;
    // us21/us22: habilidad asociada a la sesion
    private String habilidadNombre;

    // conversion desde entidad al dto (patron de mapeo)
    public static SesionResponseDto fromEntity(SesionAprendizaje s) {
        SesionResponseDto dto = new SesionResponseDto();
        dto.setSesionId(s.getSesionId());
        dto.setTitulo(s.getTitulo());
        dto.setDescripcion(s.getDescripcion());
        dto.setFechaSesion(s.getFechaSesion());
        dto.setModalidad(s.getModalidad() != null ? s.getModalidad().name() : "VIRTUAL");
        dto.setTipo(s.getTipo() != null ? s.getTipo().name() : "PUBLICA");
        dto.setEstado(s.getEstado() != null ? s.getEstado().name() : "PENDIENTE");
        dto.setMaxParticipantes(s.getMaxParticipantes());
        dto.setCapacidadMaxima(s.getMaxParticipantes());
        dto.setDuracionMinutos(60);
        dto.setLinkSesion(s.getLinkSesion());
        dto.setLugar(s.getLugar());
        dto.setUbicacion(s.getLugar() != null ? s.getLugar() : s.getLinkSesion());
        dto.setFechaCreacion(s.getFechaCreacion());
        dto.setCategoria(s.getCategoria());
        if (s.getHabilidad() != null) {
            dto.setHabilidadNombre(s.getHabilidad().getNombre());
        }
        if (s.getInstructor() != null) {
            dto.setInstructorId(s.getInstructor().getUsuarioId());
            dto.setInstructorNombre(s.getInstructor().getNombre());
        }
        return dto;
    }
}
