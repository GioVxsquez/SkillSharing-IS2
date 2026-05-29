package com.skillsharing.application.dto.response;
import com.skillsharing.domain.entity.Usuario;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Data
public class UsuarioResponseDto {
    private Long usuarioId;
    private String nombre;
    private String email;
    private String fotoPerfil;
    private String rol;
    private List<String> habilidades;  // hu22: lista de nombres de habilidades del perfil
    private LocalDateTime fechaRegistro;
    public static UsuarioResponseDto fromEntity(Usuario u) {
        UsuarioResponseDto dto = new UsuarioResponseDto();
        dto.setUsuarioId(u.getUsuarioId());
        dto.setNombre(u.getNombre());
        dto.setEmail(u.getEmail());
        dto.setFotoPerfil(u.getFotoPerfil());
        dto.setRol(u.getRol().name());
        dto.setFechaRegistro(u.getFechaRegistro());
        // hu22: mapea el set de habilidades a una lista de nombres
        if (u.getHabilidades() != null) {
            dto.setHabilidades(u.getHabilidades().stream()
                    .map(h -> h.getNombre())
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
