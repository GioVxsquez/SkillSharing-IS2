package com.skillsharing.domain.enums;

// patron state (semana 5 - patrones de comportamiento)
// modela el ciclo de vida de una sesion de aprendizaje
// en sprint 1 una sesion creada queda activa para poder ser visualizada e inscrita
public enum EstadoSesion {
    PENDIENTE,   // reservado para releases posteriores
    ACTIVA,      // visible y abierta a inscripciones
    FINALIZADA,  // la sesion ya ocurrio
    RECHAZADA    // reservado para releases posteriores
}
