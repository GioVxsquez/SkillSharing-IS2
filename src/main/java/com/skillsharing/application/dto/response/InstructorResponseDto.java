package com.skillsharing.application.dto.response;

import com.skillsharing.domain.entity.Habilidad;
import com.skillsharing.domain.entity.Usuario;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

// dto especializado para mostrar instructores con sus habilidades y reputacion
// evita exponer la entidad Usuario directamente (encapsulamiento - poo semana 1)
// us20: incluye la reputacion promedio del instructor
// us22: incluye lista de habilidades para que el aprendiz elija al instructor adecuado
@Data
public class InstructorResponseDto {

    private Long usuarioId;
    private String nombre;
    private String email;
    private String fotoPerfil;
    private List<String> habilidades;
    private Double reputacionPromedio;

    public static InstructorResponseDto fromEntity(Usuario u, Double reputacion) {
        InstructorResponseDto dto = new InstructorResponseDto();
        dto.setUsuarioId(u.getUsuarioId());
        dto.setNombre(u.getNombre());
        dto.setEmail(u.getEmail());
        dto.setFotoPerfil(u.getFotoPerfil());
        dto.setReputacionPromedio(reputacion != null ? reputacion : 0.0);
        dto.setHabilidades(
            u.getHabilidades().stream()
                .map(Habilidad::getNombre)
                .collect(Collectors.toList())
        );
        return dto;
    }
}
