package com.skillsharing.application.service;

import com.skillsharing.domain.entity.Notificacion;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

// us12/us13: sistema de notificaciones - patron observer (semana 5)
// se invoca desde InvitacionService para notificar automaticamente
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    // crear una notificacion para un usuario
    @Transactional
    public Notificacion crear(Usuario usuario, SesionAprendizaje sesion, String mensaje) {
        Notificacion notif = Notificacion.builder()
                .usuario(usuario)
                .sesion(sesion)
                .mensaje(mensaje)
                .build();
        return notificacionRepository.save(notif);
    }

    // listar todas las notificaciones de un usuario
    public List<Notificacion> listarPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    // listar solo las no vistas
    public List<Notificacion> listarNoVistas(Long usuarioId) {
        return notificacionRepository.findByUsuarioUsuarioIdAndVistoFalseOrderByFechaCreacionDesc(usuarioId);
    }

    // contar no vistas (para el badge)
    public long contarNoVistas(Long usuarioId) {
        return notificacionRepository.countByUsuarioUsuarioIdAndVistoFalse(usuarioId);
    }

    // marcar como vista
    @Transactional
    public void marcarComoVista(Long notificacionId) {
        Notificacion notif = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new RuntimeException("notificacion no encontrada"));
        notif.setVisto(true);
        notificacionRepository.save(notif);
    }

    // marcar todas como vistas
    @Transactional
    public void marcarTodasComoVistas(Long usuarioId) {
        List<Notificacion> noVistas = notificacionRepository
                .findByUsuarioUsuarioIdAndVistoFalseOrderByFechaCreacionDesc(usuarioId);
        noVistas.forEach(n -> n.setVisto(true));
        notificacionRepository.saveAll(noVistas);
    }
}
