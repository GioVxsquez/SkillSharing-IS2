package com.skillsharing.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skillsharing.domain.entity.Inscripcion;
import com.skillsharing.domain.enums.EstadoSesion;

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
}
