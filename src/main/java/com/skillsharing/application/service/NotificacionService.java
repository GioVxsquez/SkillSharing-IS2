package com.skillsharing.application.service;

import com.skillsharing.domain.entity.Invitacion;
import com.skillsharing.domain.entity.Notificacion;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public void notificarNuevaInvitacion(Usuario usuario, SesionAprendizaje sesion) {

        Notificacion notificacion = Notificacion.builder()
                .usuario(usuario)
                .sesion(sesion)
                .mensaje("Has recibido una nueva invitación para la sesión \"" + sesion.getTitulo() + "\".")
                .build();

        notificacionRepository.save(notificacion);
    }

    public void notificarRespuestaInvitacion(Invitacion invitacion) {

        String mensaje = invitacion.getEstado().name().equals("ACEPTADA")
                ? "El usuario confirmó su asistencia a la sesión \"" + invitacion.getSesion().getTitulo() + "\"."
                : "El usuario rechazó la invitación para la sesión \"" + invitacion.getSesion().getTitulo() + "\".";

        Notificacion notificacion = Notificacion.builder()
                .usuario(invitacion.getSesion().getInstructor())
                .sesion(invitacion.getSesion())
                .mensaje(mensaje)
                .build();

        notificacionRepository.save(notificacion);
    }
}