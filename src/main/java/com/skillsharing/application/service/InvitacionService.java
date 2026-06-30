package com.skillsharing.application.service;

import com.skillsharing.domain.entity.Invitacion;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.EstadoInvitacion;
import com.skillsharing.domain.enums.TipoSesion;
import com.skillsharing.infrastructure.repository.InvitacionRepository;
import com.skillsharing.infrastructure.repository.SesionRepository;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

// logica para la gestion de invitaciones
@Service
@RequiredArgsConstructor
public class InvitacionService {

    private final InvitacionRepository invitacionRepository;
    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscripcionService inscripcionService;
    private final NotificacionService notificacionService;

    // hu06: invitar asistentes
    @Transactional
    public Invitacion enviarInvitacion(Long organizadorId, Long sesionId, Long invitadoId) {
        SesionAprendizaje sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RuntimeException("sesion no encontrada"));
        
        if (!sesion.getInstructor().getUsuarioId().equals(organizadorId)) {
            throw new IllegalStateException("solo el organizador puede enviar invitaciones");
        }

        if (sesion.getTipo() != TipoSesion.PRIVADA) {
            throw new IllegalStateException("solo las sesiones privadas usan invitaciones");
        }

        if (sesion.getFechaSesion().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("no se puede invitar a una sesion que ya inicio");
        }

        Usuario invitado = usuarioRepository.findById(invitadoId)
                .orElseThrow(() -> new RuntimeException("invitado no encontrado"));

        if (invitado.getUsuarioId().equals(organizadorId)) {
            throw new IllegalStateException("no puedes invitarte a tu propia sesion");
        }

        if (invitacionRepository.existsBySesionSesionIdAndInvitadoUsuarioId(sesionId, invitadoId)) {
            throw new IllegalStateException("el usuario ya ha sido invitado (RN11)");
        }

        long invitacionesActivas = invitacionRepository.countBySesionSesionIdAndEstadoIn(
                sesionId,
                List.of(EstadoInvitacion.PENDIENTE, EstadoInvitacion.ACEPTADA)
        );
        if (invitacionesActivas >= 50) {
            throw new IllegalStateException("limite maximo de 50 invitaciones alcanzado (RN12)");
        }

        Invitacion inv = Invitacion.builder()
                .sesion(sesion)
                .invitado(invitado)
                .estado(EstadoInvitacion.PENDIENTE)
                .fechaEnvio(LocalDateTime.now())
                .build();
                
        Invitacion saved = invitacionRepository.save(inv);

        // us12: notificar al invitado
        notificacionService.crear(invitado, sesion, "Has recibido una invitacion para la sesion: " + sesion.getTitulo());

        return saved;
    }

    // hu28: visualizar invitaciones privadas
    public List<Invitacion> listarMisInvitaciones(Long usuarioId) {
        return invitacionRepository.findByInvitadoUsuarioIdAndEstado(usuarioId, EstadoInvitacion.PENDIENTE);
    }

    // hu07: confirmar asistencia privada (aceptar)
    @Transactional
    public void responderInvitacion(Long invitadoId, Long invitacionId, boolean aceptar) {
        Invitacion inv = invitacionRepository.findById(invitacionId)
                .orElseThrow(() -> new RuntimeException("invitacion no encontrada"));

        if (!inv.getInvitado().getUsuarioId().equals(invitadoId)) {
            throw new IllegalStateException("no tienes permiso para responder a esta invitacion");
        }

        if (inv.getEstado() != EstadoInvitacion.PENDIENTE) {
            throw new IllegalStateException("la invitacion ya fue respondida");
        }

        if (inv.getFechaEnvio().plusDays(5).isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("la invitacion ha expirado despues de 5 dias (RN17)");
        }

        if (inv.getSesion().getFechaSesion().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("la sesion ya inicio y no permite confirmar asistencia");
        }

        if (aceptar) {
            inv.setEstado(EstadoInvitacion.ACEPTADA);
            inscripcionService.inscribir(inv.getSesion().getSesionId(), invitadoId);
        } else {
            inv.setEstado(EstadoInvitacion.RECHAZADA);
        }
        
        Invitacion saved = invitacionRepository.save(inv);

        // us13: notificar al instructor
        String respuesta = aceptar ? "aceptado" : "rechazado";
        notificacionService.crear(
            inv.getSesion().getInstructor(),
            inv.getSesion(),
            inv.getInvitado().getNombre() + " ha " + respuesta + " tu invitacion a: " + inv.getSesion().getTitulo()
        );
    }
}
