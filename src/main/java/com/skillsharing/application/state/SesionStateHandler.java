package com.skillsharing.application.state;

import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.enums.EstadoSesion;
import org.springframework.stereotype.Component;

// patron state (semana 5 - patrones de comportamiento)
// gestiona las transiciones validas entre estados del ciclo de vida de la sesion
// encapsula las reglas de que transicion es permitida desde cada estado
//
// principio srp (semana 2): esta clase solo maneja la logica de transiciones de estado
// no guarda la sesion, no notifica - solo valida y aplica el cambio de estado
//
// relacion con diagrama de secuencia:
//   AdminController -> SesionFacade -> SesionStateHandler -> SesionAprendizaje
@Component
public class SesionStateHandler {

    // verifica si el cambio de estado es valido segun las reglas del negocio
    // transiciones permitidas:
    //   PENDIENTE -> ACTIVA    (admin aprueba, requiere material cargado - hu05)
    //   PENDIENTE -> RECHAZADA (admin rechaza)
    //   ACTIVA    -> FINALIZADA (la sesion ocurrio)
    //   FINALIZADA y RECHAZADA son estados terminales - no cambian
    public boolean puedeTransicionar(SesionAprendizaje sesion, EstadoSesion nuevoEstado) {
        return switch (sesion.getEstado()) {
            case PENDIENTE -> nuevoEstado == EstadoSesion.ACTIVA
                           || nuevoEstado == EstadoSesion.RECHAZADA;
            case ACTIVA    -> nuevoEstado == EstadoSesion.FINALIZADA;
            // estados terminales - no se puede salir de ellos
            case FINALIZADA, RECHAZADA -> false;
        };
    }

    // aplica el cambio de estado si la transicion es valida
    // lanza excepcion si no se cumplen las condiciones del negocio
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
