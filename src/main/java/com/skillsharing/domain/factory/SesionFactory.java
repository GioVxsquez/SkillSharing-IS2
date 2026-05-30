package com.skillsharing.domain.factory;

import com.skillsharing.domain.entity.Habilidad;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.domain.enums.ModalidadSesion;
import com.skillsharing.domain.enums.TipoSesion;
import java.time.LocalDateTime;

// patron factory method (semana 3 - patrones creacionales)
// centraliza la creacion de sesiones segun su tipo (virtual o presencial)
// principio ocp (semana 2): si se agrega una nueva modalidad, solo se extiende este factory
// principio srp (semana 2): la logica de construccion esta separada del servicio
public class SesionFactory {

    // crea una sesion virtual con link de videollamada
    // se usa cuando la modalidad en el dto es "VIRTUAL"
    public static SesionAprendizaje crearVirtual(
            String titulo,
            String descripcion,
            LocalDateTime fecha,
            int maxParticipantes,
            String linkSesion,
            Usuario instructor,
            Habilidad habilidad,
            TipoSesion tipo) {

        return SesionAprendizaje.builder()
                .titulo(titulo)
                .descripcion(descripcion)
                .fechaSesion(fecha)
                .modalidad(ModalidadSesion.VIRTUAL)
                .estado(EstadoSesion.PENDIENTE)
                .tipo(tipo)
                .maxParticipantes(maxParticipantes)
                .linkSesion(linkSesion)
                .instructor(instructor)
                .habilidadRequerida(habilidad)
                .materialCargado(false)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    // crea una sesion presencial con lugar fisico
    // se usa cuando la modalidad en el dto es "PRESENCIAL"
    public static SesionAprendizaje crearPresencial(
            String titulo,
            String descripcion,
            LocalDateTime fecha,
            int maxParticipantes,
            String lugar,
            Usuario instructor,
            Habilidad habilidad,
            TipoSesion tipo) {

        return SesionAprendizaje.builder()
                .titulo(titulo)
                .descripcion(descripcion)
                .fechaSesion(fecha)
                .modalidad(ModalidadSesion.PRESENCIAL)
                .estado(EstadoSesion.PENDIENTE)
                .tipo(tipo)
                .maxParticipantes(maxParticipantes)
                .lugar(lugar)
                .instructor(instructor)
                .habilidadRequerida(habilidad)
                .materialCargado(false)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }
}
