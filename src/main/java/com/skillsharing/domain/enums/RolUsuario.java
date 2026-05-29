package com.skillsharing.domain.enums;

// los roles del sistema definen que puede hacer cada usuario
// aprendiz: puede inscribirse a sesiones y ver materiales
// instructor: puede crear sesiones y subir materiales educativos (hu05)
// admin: puede aprobar o rechazar sesiones pendientes (hu10)
public enum RolUsuario {
    APRENDIZ,
    INSTRUCTOR,
    ADMIN
}
