package com.skillsharing.application.service;

import com.skillsharing.application.dto.request.SesionRequestDto;
import com.skillsharing.domain.entity.Habilidad;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.domain.factory.SesionFactory;
import com.skillsharing.infrastructure.repository.HabilidadRepository;
import com.skillsharing.infrastructure.repository.SesionRepository;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

// servicio central de sesiones (hu06, hu07, hu08, hu11)
// principio srp (semana 2): gestiona logica de negocio de sesiones, pero delega el cambio de estado al facade
//
// relacion con diagrama de secuencia:
//   SesionController -> SesionService -> [SesionRepository, SesionFactory]
@Service
@RequiredArgsConstructor
public class SesionService {

    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final HabilidadRepository habilidadRepository;

    // hu06: crear sesion de aprendizaje
    // usa el patron factory (semana 3) para decidir que tipo de sesion instanciar
    @Transactional
    public SesionAprendizaje crearSesion(Long instructorId, SesionRequestDto dto) {
        Usuario instructor = usuarioRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("instructor no encontrado"));

        Habilidad habilidad = null;
        if (dto.getHabilidadId() != null) {
            habilidad = habilidadRepository.findById(dto.getHabilidadId())
                    .orElseThrow(() -> new RuntimeException("habilidad requerida no encontrada"));
        }

        SesionAprendizaje nuevaSesion;
        // patron factory method
        if ("VIRTUAL".equalsIgnoreCase(dto.getModalidad())) {
            nuevaSesion = SesionFactory.crearVirtual(
                    dto.getTitulo(), dto.getDescripcion(), dto.getFechaSesion(),
                    dto.getMaxParticipantes(), dto.getLinkSesion(), instructor, habilidad
            );
        } else if ("PRESENCIAL".equalsIgnoreCase(dto.getModalidad())) {
            nuevaSesion = SesionFactory.crearPresencial(
                    dto.getTitulo(), dto.getDescripcion(), dto.getFechaSesion(),
                    dto.getMaxParticipantes(), dto.getLugar(), instructor, habilidad
            );
        } else {
            throw new IllegalArgumentException("modalidad invalida");
        }

        return sesionRepository.save(nuevaSesion);
    }

    // hu07: listar sesiones activas (aprobadas)
    public List<SesionAprendizaje> listarActivas() {
        return sesionRepository.findByEstado(EstadoSesion.ACTIVA);
    }

    // hu08: ver detalle de sesion
    public SesionAprendizaje obtenerDetalle(Long sesionId) {
        return sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RuntimeException("sesion no encontrada"));
    }

    // hu11: ver mis sesiones (como instructor)
    public List<SesionAprendizaje> listarPorInstructor(Long instructorId) {
        return sesionRepository.findByInstructorUsuarioId(instructorId);
    }
}
