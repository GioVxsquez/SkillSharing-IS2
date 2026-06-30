package com.skillsharing.infrastructure.repository;

import com.skillsharing.domain.entity.Inscripcion;
import com.skillsharing.domain.enums.EstadoSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    List<Inscripcion> findBySesionSesionId(Long sesionId);
    List<Inscripcion> findByUsuarioUsuarioId(Long usuarioId);
    Optional<Inscripcion> findBySesionSesionIdAndUsuarioUsuarioId(Long sesionId, Long usuarioId);
    boolean existsBySesionSesionIdAndUsuarioUsuarioId(Long sesionId, Long usuarioId);

    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.sesion.sesionId = :sesionId")
    long countBySesionId(@Param("sesionId") Long sesionId);

    @Query("SELECT COUNT(i) FROM Inscripcion i " +
           "WHERE i.usuario.usuarioId = :usuarioId " +
           "AND i.sesion.estado IN :estados " +
           "AND i.sesion.fechaSesion > :fecha")
    long countActivasByUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("estados") List<EstadoSesion> estados,
            @Param("fecha") LocalDateTime fecha);

    @Query("SELECT i FROM Inscripcion i " +
           "WHERE i.usuario.usuarioId = :usuarioId " +
           "AND i.sesion.fechaSesion >= :limite " +
           "ORDER BY i.sesion.fechaSesion ASC")
    List<Inscripcion> findVigentesByUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("limite") LocalDateTime limite);

    // us25: validar cruce de horarios - buscar inscripciones en ventana de tiempo
    @Query("SELECT i FROM Inscripcion i WHERE i.usuario.usuarioId = :usuarioId " +
           "AND i.sesion.estado IN ('ACTIVA', 'PENDIENTE') " +
           "AND i.sesion.fechaSesion BETWEEN :desde AND :hasta")
    List<Inscripcion> findConflictoHorario(
            @Param("usuarioId") Long usuarioId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
