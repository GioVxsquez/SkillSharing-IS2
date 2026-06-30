package com.skillsharing.infrastructure.repository;

import com.skillsharing.domain.entity.Habilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HabilidadRepository extends JpaRepository<Habilidad, Long> {
    List<Habilidad> findByNombreContainingIgnoreCase(String nombre);
}
