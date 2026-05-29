package com.skillsharing.application.dto.response;
import com.skillsharing.domain.entity.SesionAprendizaje;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class SesionResponseDto {
    private Long sesionId;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaSesion;
    private String modalidad;
    private String estado;
    private Integer maxParticipantes;
    private String linkSesion;
    private String lugar;
    private Long instructorId;
    private String instructorNombre;
    private String habilidadRequerida;
    private Boolean materialCargado;
    private LocalDateTime fechaCreacion;
    private long totalInscritos;
    public static SesionResponseDto fromEntity(SesionAprendizaje s) {
        SesionResponseDto dto = new SesionResponseDto();
        dto.setSesionId(s.getSesionId());
        dto.setTitulo(s.getTitulo());
        dto.setDescripcion(s.getDescripcion());
        dto.setFechaSesion(s.getFechaSesion());
        dto.setModalidad(s.getModalidad().name());
        dto.setEstado(s.getEstado().name());
        dto.setMaxParticipantes(s.getMaxParticipantes());
        dto.setLinkSesion(s.getLinkSesion());
        dto.setLugar(s.getLugar());
        dto.setMaterialCargado(s.getMaterialCargado());
        dto.setFechaCreacion(s.getFechaCreacion());
        if (s.getInstructor() != null) {
            dto.setInstructorId(s.getInstructor().getUsuarioId());
            dto.setInstructorNombre(s.getInstructor().getNombre());
        }
        if (s.getHabilidadRequerida() != null) {
            dto.setHabilidadRequerida(s.getHabilidadRequerida().getNombre());
        }
        return dto;
    }
}
