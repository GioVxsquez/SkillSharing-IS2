package com.skillsharing.infrastructure.repository;

import com.skillsharing.domain.entity.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    List<Calificacion> findBySesionSesionId(Long sesionId);
    Optional<Calificacion> findBySesionSesionIdAndUsuarioUsuarioId(Long sesionId, Long usuarioId);
    boolean existsBySesionSesionIdAndUsuarioUsuarioId(Long sesionId, Long usuarioId);

    // promedio de puntuacion de todas las sesiones de un instructor - us20
    @Query("SELECT AVG(c.puntuacion) FROM Calificacion c WHERE c.sesion.instructor.usuarioId = :instructorId")
    Double calcularReputacionInstructor(@Param("instructorId") Long instructorId);
}
