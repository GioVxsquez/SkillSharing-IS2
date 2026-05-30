package com.skillsharing.application.service;

import com.skillsharing.application.dto.request.SesionRequestDto;
import com.skillsharing.domain.entity.Habilidad;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.domain.enums.TipoSesion;
import com.skillsharing.domain.factory.SesionFactory;
import com.skillsharing.infrastructure.repository.HabilidadRepository;
import com.skillsharing.infrastructure.repository.SesionRepository;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

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

        if (sesionRepository.existsByTituloIgnoreCase(dto.getTitulo().trim())) {
            throw new IllegalStateException("ya existe una sesion con ese titulo");
        }

        long sesionesAbiertas = sesionRepository.countByInstructorUsuarioIdAndEstadoIn(
                instructorId,
                List.of(EstadoSesion.PENDIENTE, EstadoSesion.ACTIVA)
        );
        if (sesionesAbiertas >= 5) {
            throw new IllegalStateException("alcanzaste el limite de 5 sesiones activas creadas");
        }

        Habilidad habilidad = null;
        if (dto.getHabilidadId() != null) {
            habilidad = habilidadRepository.findById(dto.getHabilidadId())
                    .orElseThrow(() -> new RuntimeException("habilidad requerida no encontrada"));
        }

        int maxParticipantes = dto.getMaxParticipantes() == null ? 20 : dto.getMaxParticipantes();
        if (maxParticipantes < 1 || maxParticipantes > 100) {
            throw new IllegalArgumentException("la capacidad debe estar entre 1 y 100 participantes");
        }

        TipoSesion tipo = resolverTipo(dto);

        SesionAprendizaje nuevaSesion;
        // patron factory method
        if ("VIRTUAL".equalsIgnoreCase(dto.getModalidad())) {
            nuevaSesion = SesionFactory.crearVirtual(
                    dto.getTitulo(), dto.getDescripcion(), dto.getFechaSesion(),
                    maxParticipantes, dto.getLinkSesion(), instructor, habilidad, tipo
            );
        } else if ("PRESENCIAL".equalsIgnoreCase(dto.getModalidad())) {
            nuevaSesion = SesionFactory.crearPresencial(
                    dto.getTitulo(), dto.getDescripcion(), dto.getFechaSesion(),
                    maxParticipantes, dto.getLugar(), instructor, habilidad, tipo
            );
        } else {
            throw new IllegalArgumentException("modalidad invalida");
        }

        return sesionRepository.save(nuevaSesion);
    }

    // hu07: listar sesiones activas (aprobadas)
    public List<SesionAprendizaje> listarActivas() {
        return sesionRepository.findByEstadoAndTipoAndFechaSesionAfterOrderByFechaSesionAsc(
                EstadoSesion.ACTIVA,
                TipoSesion.PUBLICA,
                LocalDateTime.now()
        );
    }

    // hu08: ver detalle de sesion
    public SesionAprendizaje obtenerDetalle(Long sesionId) {
        return sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RuntimeException("sesion no encontrada"));
    }

    // hu11: ver mis sesiones (como instructor)
    public List<SesionAprendizaje> listarPorInstructor(Long instructorId) {
        return sesionRepository.findByInstructorUsuarioIdAndFechaSesionAfterOrderByFechaSesionAsc(
                instructorId,
                LocalDateTime.now().minusDays(2)
        );
    }

    private TipoSesion resolverTipo(SesionRequestDto dto) {
        if (Boolean.TRUE.equals(dto.getPrivada())) {
            return TipoSesion.PRIVADA;
        }

        String tipoTexto = dto.getTipo() == null ? "PUBLICA" : dto.getTipo().trim();
        if (tipoTexto.isBlank()) {
            return TipoSesion.PUBLICA;
        }

        try {
            return TipoSesion.valueOf(tipoTexto.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("tipo de sesion invalido: usa PUBLICA o PRIVADA");
        }
    }
}
