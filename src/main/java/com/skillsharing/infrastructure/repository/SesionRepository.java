package com.skillsharing.infrastructure.repository;

import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.domain.enums.TipoSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

// principio dip (semana 2): sesionservice depende de esta interfaz
// spring data genera sql a partir del nombre del metodo (derived queries)
@Repository
public interface SesionRepository extends JpaRepository<SesionAprendizaje, Long> {

    List<SesionAprendizaje> findByEstado(EstadoSesion estado);

    List<SesionAprendizaje> findByInstructorUsuarioId(Long instructorId);

    boolean existsByTituloIgnoreCase(String titulo);

    long countByInstructorUsuarioIdAndEstadoIn(Long instructorId, List<EstadoSesion> estados);

    List<SesionAprendizaje> findByEstadoAndTipoAndFechaSesionAfterOrderByFechaSesionAsc(
            EstadoSesion estado,
            TipoSesion tipo,
            LocalDateTime fecha);

    List<SesionAprendizaje> findByInstructorUsuarioIdAndFechaSesionAfterOrderByFechaSesionAsc(
            Long instructorId,
            LocalDateTime fecha);

}
