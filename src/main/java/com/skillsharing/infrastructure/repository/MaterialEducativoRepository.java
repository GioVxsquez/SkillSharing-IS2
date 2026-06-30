package com.skillsharing.infrastructure.repository;

import com.skillsharing.domain.entity.MaterialEducativo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaterialEducativoRepository extends JpaRepository<MaterialEducativo, Long> {
    List<MaterialEducativo> findBySesionSesionId(Long sesionId);
    void deleteBySesionSesionId(Long sesionId);
}
