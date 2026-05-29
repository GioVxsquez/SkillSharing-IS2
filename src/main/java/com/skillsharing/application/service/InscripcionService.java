package com.skillsharing.application.service;

import com.skillsharing.application.observer.SesionObserver;
import com.skillsharing.domain.entity.Inscripcion;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.infrastructure.repository.InscripcionRepository;
import com.skillsharing.infrastructure.repository.SesionRepository;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

// hu09: logica de inscripcion de usuarios a sesiones
@Service
@RequiredArgsConstructor
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final List<SesionObserver> observadores; // para notificar inscripcion

    @Transactional
    public Inscripcion inscribir(Long sesionId, Long usuarioId) {
        SesionAprendizaje sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RuntimeException("sesion no encontrada"));
                
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        // validaciones del negocio
        if (sesion.getEstado() != EstadoSesion.ACTIVA) {
            throw new IllegalStateException("solo puedes inscribirte a sesiones activas");
        }

        if (sesion.getInstructor().getUsuarioId().equals(usuarioId)) {
            throw new IllegalStateException("el instructor no puede inscribirse a su propia sesion");
        }

        if (inscripcionRepository.existsBySesionSesionIdAndUsuarioUsuarioId(sesionId, usuarioId)) {
            throw new IllegalStateException("el usuario ya esta inscrito en esta sesion");
        }

        long totalInscritos = inscripcionRepository.countBySesionId(sesionId);
        if (totalInscritos >= sesion.getMaxParticipantes()) {
            throw new IllegalStateException("la sesion ha alcanzado el maximo de participantes");
        }

        Inscripcion inscripcion = Inscripcion.builder()
                .sesion(sesion)
                .usuario(usuario)
                .rolSesion("APRENDIZ")
                .build();
                
        Inscripcion guardada = inscripcionRepository.save(inscripcion);

        // notificar al instructor (patron observer)
        for (SesionObserver obs : observadores) {
            obs.onSesionActualizada(sesion, "INSCRIPCION");
        }

        return guardada;
    }

    public List<Inscripcion> listarPorUsuario(Long usuarioId) {
        return inscripcionRepository.findByUsuarioUsuarioId(usuarioId);
    }
}
