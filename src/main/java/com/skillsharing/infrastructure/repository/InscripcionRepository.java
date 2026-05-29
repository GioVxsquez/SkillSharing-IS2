package com.skillsharing.infrastructure.repository;

import com.skillsharing.domain.entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    List<Inscripcion> findBySesionSesionId(Long sesionId);
    List<Inscripcion> findByUsuarioUsuarioId(Long usuarioId);
    Optional<Inscripcion> findBySesionSesionIdAndUsuarioUsuarioId(Long sesionId, Long usuarioId);
    boolean existsBySesionSesionIdAndUsuarioUsuarioId(Long sesionId, Long usuarioId);

    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.sesion.sesionId = :sesionId")
    long countBySesionId(@Param("sesionId") Long sesionId);
}
