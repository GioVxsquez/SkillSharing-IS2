package com.skillsharing.application.dto.response;

import com.skillsharing.domain.entity.Usuario;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// dto de respuesta para usuarios
// nunca expone el campo password - esto es encapsulamiento (poo semana 1)
@Data
public class UsuarioResponseDto {

    private Long usuarioId;
    private String nombre;
    private String email;
    private String fotoPerfil;
    private String rol;
    private LocalDateTime fechaRegistro;
    // us21/us22: habilidades del perfil para mostrar en la app
    private List<HabilidadResponseDto> habilidades;

    // conversion desde entidad al dto
    public static UsuarioResponseDto fromEntity(Usuario u) {
        UsuarioResponseDto dto = new UsuarioResponseDto();
        dto.setUsuarioId(u.getUsuarioId());
        dto.setNombre(u.getNombre());
        dto.setEmail(u.getEmail());
        dto.setFotoPerfil(u.getFotoPerfil());
        dto.setRol(u.getRol() != null ? u.getRol().name() : "APRENDIZ");
        dto.setFechaRegistro(u.getFechaRegistro());
        dto.setHabilidades(
            u.getHabilidades() != null
                ? u.getHabilidades().stream().map(HabilidadResponseDto::fromEntity).collect(Collectors.toList())
                : List.of()
        );
        return dto;
    }
}
