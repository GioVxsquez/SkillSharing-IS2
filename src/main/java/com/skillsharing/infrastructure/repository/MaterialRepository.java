package com.skillsharing.infrastructure.repository;
import com.skillsharing.domain.entity.MaterialEducativo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
// hu05: busca materiales por sesion para verificar que existe al menos uno
@Repository
public interface MaterialRepository extends JpaRepository<MaterialEducativo, Long> {
    List<MaterialEducativo> findBySesionSesionId(Long sesionId);
    boolean existsBySesionSesionId(Long sesionId);
}
