package com.skillsharing.infrastructure.repository;

import com.skillsharing.domain.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// hu12: obtiene notificaciones de un usuario ordenadas por fecha descendente
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    List<Notificacion> findByUsuarioUsuarioIdAndVistoFalse(Long usuarioId);
}
