package com.skillsharing.domain.factory;
import com.skillsharing.domain.entity.Habilidad;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.domain.enums.ModalidadSesion;
import java.time.LocalDateTime;
public class SesionFactory {
    public static SesionAprendizaje crearVirtual(
            String titulo,
            String descripcion,
            LocalDateTime fecha,
            int maxParticipantes,
            String linkSesion,
            Usuario instructor,
            Habilidad habilidad) {
        return SesionAprendizaje.builder()
                .titulo(titulo)
                .descripcion(descripcion)
                .fechaSesion(fecha)
                .modalidad(ModalidadSesion.VIRTUAL)
                .estado(EstadoSesion.PENDIENTE)
                .maxParticipantes(maxParticipantes)
                .linkSesion(linkSesion)
                .instructor(instructor)
                .habilidadRequerida(habilidad)
                .materialCargado(false)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }
    public static SesionAprendizaje crearPresencial(
            String titulo,
            String descripcion,
            LocalDateTime fecha,
            int maxParticipantes,
            String lugar,
            Usuario instructor,
            Habilidad habilidad) {
        return SesionAprendizaje.builder()
                .titulo(titulo)
                .descripcion(descripcion)
                .fechaSesion(fecha)
                .modalidad(ModalidadSesion.PRESENCIAL)
                .estado(EstadoSesion.PENDIENTE)
                .maxParticipantes(maxParticipantes)
                .lugar(lugar)
                .instructor(instructor)
                .habilidadRequerida(habilidad)
                .materialCargado(false)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }
}
