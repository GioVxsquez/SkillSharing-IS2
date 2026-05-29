package com.skillsharing.application.state;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.enums.EstadoSesion;
import org.springframework.stereotype.Component;
@Component
public class SesionStateHandler {
    //   PENDIENTE -> ACTIVA    (admin aprueba, requiere material cargado - hu05)
    public boolean puedeTransicionar(SesionAprendizaje sesion, EstadoSesion nuevoEstado) {
        return switch (sesion.getEstado()) {
            case PENDIENTE -> nuevoEstado == EstadoSesion.ACTIVA
                           || nuevoEstado == EstadoSesion.RECHAZADA;
            case ACTIVA    -> nuevoEstado == EstadoSesion.FINALIZADA;
            case FINALIZADA, RECHAZADA -> false;
        };
    }
    public void aplicarTransicion(SesionAprendizaje sesion, EstadoSesion nuevoEstado) {
        if (!puedeTransicionar(sesion, nuevoEstado)) {
            throw new IllegalStateException(
                "transicion invalida: " + sesion.getEstado() + " -> " + nuevoEstado
            );
        }
        // hu05: validacion critica - no se puede aprobar sin material educativo cargado
        if (nuevoEstado == EstadoSesion.ACTIVA && Boolean.FALSE.equals(sesion.getMaterialCargado())) {
            throw new IllegalStateException(
                "la sesion no puede aprobarse sin material educativo (hu05) - sube un PDF primero"
            );
        }
        sesion.setEstado(nuevoEstado);
    }
}
