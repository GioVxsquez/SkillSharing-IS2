package com.skillsharing.application.facade;
import com.skillsharing.application.observer.SesionObserver;
import com.skillsharing.application.state.SesionStateHandler;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.infrastructure.repository.SesionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@RequiredArgsConstructor
public class SesionFacade {
    private final SesionRepository sesionRepository;
    private final SesionStateHandler stateHandler;
    private final List<SesionObserver> observadores;
    // hu10: admin aprueba o rechaza una sesion pendiente
    @Transactional
    public SesionAprendizaje cambiarEstado(Long sesionId, EstadoSesion nuevoEstado) {
        SesionAprendizaje sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RuntimeException("sesion no encontrada: " + sesionId));
        stateHandler.aplicarTransicion(sesion, nuevoEstado);
        SesionAprendizaje guardada = sesionRepository.save(sesion);
        String evento = nuevoEstado == EstadoSesion.ACTIVA ? "APROBADA" : nuevoEstado.name();
        for (SesionObserver obs : observadores) {
            obs.onSesionActualizada(guardada, evento);
        }
        return guardada;
    }
}
