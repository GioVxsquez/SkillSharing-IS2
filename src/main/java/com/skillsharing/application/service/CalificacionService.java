package com.skillsharing.application.service;

import com.skillsharing.domain.entity.Calificacion;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.infrastructure.repository.CalificacionRepository;
import com.skillsharing.infrastructure.repository.InscripcionRepository;
import com.skillsharing.infrastructure.repository.SesionRepository;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

// us19: calificar sesion finalizada con puntuacion 1-5
// us20: sistema de reputacion del instructor basado en promedio de calificaciones
@Service
@RequiredArgsConstructor
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscripcionRepository inscripcionRepository;

    // us19: calificar una sesion
    @Transactional
    public Calificacion calificar(Long sesionId, Long usuarioId, Integer puntuacion, String comentario) {
        SesionAprendizaje sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RuntimeException("sesion no encontrada"));

        if (sesion.getEstado() != EstadoSesion.FINALIZADA) {
            throw new IllegalStateException("solo se pueden calificar sesiones finalizadas");
        }

        if (!inscripcionRepository.existsBySesionSesionIdAndUsuarioUsuarioId(sesionId, usuarioId)) {
            throw new IllegalStateException("solo los asistentes inscritos pueden calificar la sesion");
        }

        if (calificacionRepository.existsBySesionSesionIdAndUsuarioUsuarioId(sesionId, usuarioId)) {
            throw new IllegalStateException("ya calificaste esta sesion");
        }

        if (puntuacion < 1 || puntuacion > 5) {
            throw new IllegalArgumentException("la puntuacion debe ser entre 1 y 5 estrellas");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        Calificacion cal = Calificacion.builder()
                .sesion(sesion)
                .usuario(usuario)
                .puntuacion(puntuacion)
                .comentario(comentario)
                .build();

        return calificacionRepository.save(cal);
    }

    // listar calificaciones de una sesion
    public List<Calificacion> listarPorSesion(Long sesionId) {
        return calificacionRepository.findBySesionSesionId(sesionId);
    }

    // us20: reputacion del instructor (promedio de todas sus sesiones)
    public Double obtenerReputacion(Long instructorId) {
        Double reputacion = calificacionRepository.calcularReputacionInstructor(instructorId);
        return reputacion != null ? Math.round(reputacion * 10.0) / 10.0 : 0.0;
    }
}
