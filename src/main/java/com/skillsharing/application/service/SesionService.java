package com.skillsharing.application.service;

import com.skillsharing.application.dto.request.SesionRequestDto;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.domain.enums.TipoSesion;
import com.skillsharing.domain.factory.SesionFactory;
import com.skillsharing.infrastructure.repository.SesionRepository;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

// servicio central de sesiones del sprint 1
// principio srp (semana 2): concentra la logica de negocio de sesiones
//
// relacion con diagrama de secuencia:
//   SesionController -> SesionService -> [SesionRepository, SesionFactory]
@Service
@RequiredArgsConstructor
public class SesionService {

    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;

    // hu01: crear sesion de aprendizaje
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
                List.of(EstadoSesion.ACTIVA)
        );
        if (sesionesAbiertas >= 5) {
            throw new IllegalStateException("alcanzaste el limite de 5 sesiones activas creadas");
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
                    maxParticipantes, dto.getLinkSesion(), instructor, tipo
            );
        } else if ("PRESENCIAL".equalsIgnoreCase(dto.getModalidad())) {
            nuevaSesion = SesionFactory.crearPresencial(
                    dto.getTitulo(), dto.getDescripcion(), dto.getFechaSesion(),
                    maxParticipantes, dto.getLugar(), instructor, tipo
            );
        } else {
            throw new IllegalArgumentException("modalidad invalida");
        }

        // us09: guardar la categoria enviada por el instructor
        if (dto.getCategoria() != null && !dto.getCategoria().isBlank()) {
            nuevaSesion.setCategoria(dto.getCategoria().trim());
        }

        return sesionRepository.save(nuevaSesion);
    }

    // hu16: listar sesiones publicas activas
    public List<SesionAprendizaje> listarActivas() {
        return sesionRepository.findByEstadoAndTipoAndFechaSesionAfterOrderByFechaSesionAsc(
                EstadoSesion.ACTIVA,
                TipoSesion.PUBLICA,
                LocalDateTime.now()
        );
    }

    // hu04: ver detalle de sesion
    public SesionAprendizaje obtenerDetalle(Long sesionId) {
        return sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RuntimeException("sesion no encontrada"));
    }

    // hu02: ver mis sesiones como instructor
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

    // us03: buscador de sesiones por nombre/titulo
    public List<SesionAprendizaje> buscarPorNombre(String query) {
        return sesionRepository.findByTituloContainingIgnoreCaseAndEstadoAndTipo(
                query, EstadoSesion.ACTIVA, TipoSesion.PUBLICA);
    }

    // us08: cancelar una sesion (cambia estado a CANCELADA)
    @Transactional
    public SesionAprendizaje cancelarSesion(Long sesionId, Long instructorId) {
        SesionAprendizaje sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RuntimeException("sesion no encontrada"));

        if (!sesion.getInstructor().getUsuarioId().equals(instructorId)) {
            throw new SecurityException("solo el instructor dueno puede cancelar la sesion");
        }

        if (sesion.getEstado() == EstadoSesion.FINALIZADA || sesion.getEstado() == EstadoSesion.CANCELADA) {
            throw new IllegalStateException("no se puede cancelar una sesion ya finalizada o cancelada");
        }

        sesion.setEstado(EstadoSesion.CANCELADA);
        return sesionRepository.save(sesion);
    }

    // us09: filtrar sesiones por categoria
    public List<SesionAprendizaje> filtrarPorCategoria(String categoria) {
        return sesionRepository.findByCategoriaIgnoreCaseAndEstadoAndTipoAndFechaSesionAfterOrderByFechaSesionAsc(
                categoria, EstadoSesion.ACTIVA, TipoSesion.PUBLICA, LocalDateTime.now());
    }

    // us11: filtrar sesiones por modalidad (VIRTUAL o PRESENCIAL)
    public List<SesionAprendizaje> filtrarPorModalidad(String modalidad) {
        com.skillsharing.domain.enums.ModalidadSesion mod;
        try {
            mod = com.skillsharing.domain.enums.ModalidadSesion.valueOf(modalidad.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("modalidad invalida. Usa VIRTUAL o PRESENCIAL");
        }
        return sesionRepository.findByModalidadAndEstadoAndTipoAndFechaSesionAfterOrderByFechaSesionAsc(
                mod, EstadoSesion.ACTIVA, TipoSesion.PUBLICA, LocalDateTime.now());
    }
}
