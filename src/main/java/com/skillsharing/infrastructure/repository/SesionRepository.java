package com.skillsharing.infrastructure.repository;

import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.enums.EstadoSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

// principio dip (semana 2): sesionservice depende de esta interfaz
// spring data genera sql a partir del nombre del metodo (derived queries)
// hu28: los metodos de busqueda por habilidad se definen aqui
@Repository
public interface SesionRepository extends JpaRepository<SesionAprendizaje, Long> {

    List<SesionAprendizaje> findByEstado(EstadoSesion estado);

    List<SesionAprendizaje> findByInstructorUsuarioId(Long instructorId);

    // hu28: busca sesiones activas cuya habilidad requerida coincide con el criterio
    // usa like para busqueda parcial (ej: "prog" encuentra "programacion")
    @Query("SELECT s FROM SesionAprendizaje s " +
           "WHERE s.estado = 'ACTIVA' " +
           "AND LOWER(s.habilidadRequerida.nombre) LIKE LOWER(CONCAT('%', :habilidad, '%'))")
    List<SesionAprendizaje> findActivasByHabilidad(@Param("habilidad") String habilidad);

    // busqueda por titulo para el buscador general
    @Query("SELECT s FROM SesionAprendizaje s " +
           "WHERE s.estado = 'ACTIVA' " +
           "AND LOWER(s.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    List<SesionAprendizaje> findActivasByTitulo(@Param("titulo") String titulo);
}
