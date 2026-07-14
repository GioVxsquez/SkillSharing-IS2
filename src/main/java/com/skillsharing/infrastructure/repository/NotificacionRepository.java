package com.skillsharing.infrastructure.repository;

import com.skillsharing.domain.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
}