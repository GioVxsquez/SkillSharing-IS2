package com.skillsharing.application.dto.response;

import com.skillsharing.domain.entity.Invitacion;
import lombok.Data;

import java.time.LocalDateTime;

// dto para invitaciones privadas del release 1
// hu28: evita exponer entidades jpa completas al aplicativo movil
@Data
public class InvitacionResponseDto {

    private Long invitacionId;
    private Long sesionId;
    private String sesionTitulo;
    private String emisorNombre;
    private String invitadoNombre;
    private LocalDateTime fechaSesion;
    private String estado;

    public static InvitacionResponseDto fromEntity(Invitacion invitacion) {
        InvitacionResponseDto dto = new InvitacionResponseDto();
        dto.setInvitacionId(invitacion.getInvitacionId());
        dto.setEstado(invitacion.getEstado().name());

        if (invitacion.getSesion() != null) {
            dto.setSesionId(invitacion.getSesion().getSesionId());
            dto.setSesionTitulo(invitacion.getSesion().getTitulo());
            dto.setFechaSesion(invitacion.getSesion().getFechaSesion());
            if (invitacion.getSesion().getInstructor() != null) {
                dto.setEmisorNombre(invitacion.getSesion().getInstructor().getNombre());
            }
        }

        if (invitacion.getInvitado() != null) {
            dto.setInvitadoNombre(invitacion.getInvitado().getNombre());
        }

        return dto;
    }
}
