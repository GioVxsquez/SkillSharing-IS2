package com.skillsharing.infrastructure.repository;

import com.skillsharing.domain.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    List<Notificacion> findByUsuarioUsuarioIdAndVistoFalseOrderByFechaCreacionDesc(Long usuarioId);
    long countByUsuarioUsuarioIdAndVistoFalse(Long usuarioId);
}
