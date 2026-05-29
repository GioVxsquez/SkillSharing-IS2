package com.skillsharing.application.service;
import com.skillsharing.domain.entity.Notificacion;
import com.skillsharing.infrastructure.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
// servicio para el sistema de notificaciones (hu12)
@Service
@RequiredArgsConstructor
public class NotificacionService {
    private final NotificacionRepository notificacionRepository;
    public List<Notificacion> listarPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }
    public List<Notificacion> listarNoLeidas(Long usuarioId) {
        return notificacionRepository.findByUsuarioUsuarioIdAndVistoFalse(usuarioId);
    }
    @Transactional
    public void marcarComoLeida(Long notificacionId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new RuntimeException("notificacion no encontrada"));
        notificacion.setVisto(true);
        notificacionRepository.save(notificacion);
    }
}
