package com.skillsharing.application.dto.response;

import com.skillsharing.domain.entity.Habilidad;
import lombok.Data;

// dto de respuesta para habilidades - us21/us22
@Data
public class HabilidadResponseDto {

    private Long habilidadId;
    private String nombre;
    private String descripcion;

    public static HabilidadResponseDto fromEntity(Habilidad h) {
        HabilidadResponseDto dto = new HabilidadResponseDto();
        dto.setHabilidadId(h.getHabilidadId());
        dto.setNombre(h.getNombre());
        dto.setDescripcion(h.getDescripcion());
        return dto;
    }
}
