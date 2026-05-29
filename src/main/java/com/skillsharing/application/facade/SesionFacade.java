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

// patron facade (semana 4 - patrones estructurales)
// simplifica la interaccion entre el cambio de estado, el repositorio y los observadores
// el controller solo llama a un metodo de la facade sin conocer la complejidad interna
//
// sin facade, el controller tendria que:
//   1. buscar la sesion en el repo
//   2. llamar al state handler
//   3. guardar en el repo
//   4. recorrer todos los observadores y notificarlos
// con facade, el controller solo llama a cambiarEstado()
//
// relacion con diagrama de componentes:
//   AdminController -> SesionFacade -> [SesionStateHandler, SesionRepository, SesionObserver[]]
@Service
@RequiredArgsConstructor
public class SesionFacade {

    private final SesionRepository sesionRepository;
    private final SesionStateHandler stateHandler;
    // spring inyecta todas las implementaciones de SesionObserver como lista
    // principio ocp (semana 2): agregar un nuevo observer no requiere cambiar la facade
    private final List<SesionObserver> observadores;

    // coordina el cambio de estado de una sesion y notifica a todos los observadores
    // hu10: admin aprueba o rechaza una sesion pendiente
    @Transactional
    public SesionAprendizaje cambiarEstado(Long sesionId, EstadoSesion nuevoEstado) {
        SesionAprendizaje sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RuntimeException("sesion no encontrada: " + sesionId));

        // patron state: valida y aplica la transicion de estado
        stateHandler.aplicarTransicion(sesion, nuevoEstado);
        SesionAprendizaje guardada = sesionRepository.save(sesion);

        // patron observer: notifica a cada observador registrado
        String evento = nuevoEstado == EstadoSesion.ACTIVA ? "APROBADA" : nuevoEstado.name();
        for (SesionObserver obs : observadores) {
            obs.onSesionActualizada(guardada, evento);
        }

        return guardada;
    }
}
