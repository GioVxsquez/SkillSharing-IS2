package com.skillsharing.infrastructure.repository;
import com.skillsharing.domain.entity.Invitacion;
import com.skillsharing.domain.enums.EstadoInvitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface InvitacionRepository extends JpaRepository<Invitacion, Long> {
    // hu28: listar invitaciones privadas por usuario
    List<Invitacion> findByInvitadoUsuarioIdAndEstado(Long invitadoId, EstadoInvitacion estado);
    boolean existsBySesionSesionIdAndInvitadoUsuarioId(Long sesionId, Long invitadoId);
    long countBySesionSesionId(Long sesionId);
}
