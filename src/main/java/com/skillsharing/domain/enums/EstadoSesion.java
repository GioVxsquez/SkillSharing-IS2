package com.skillsharing.domain.enums;

// patron state (semana 5 - patrones de comportamiento)
// modela el ciclo de vida de una sesion de aprendizaje
// la sesion sigue un camino: pendiente -> activa -> finalizada (o rechazada)
// el admin aprueba la sesion solo si ya hay material educativo cargado (hu05)
public enum EstadoSesion {
    PENDIENTE,   // recien creada, espera revision del admin y carga del material
    ACTIVA,      // aprobada por admin, visible y abierta a inscripciones
    FINALIZADA,  // la sesion ya ocurrio
    RECHAZADA    // admin la rechazo por incumplimiento de requisitos
}
