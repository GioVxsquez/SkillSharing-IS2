package com.skillsharing.application.observer;

import com.skillsharing.domain.entity.Inscripcion;
import com.skillsharing.domain.entity.Notificacion;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.infrastructure.repository.InscripcionRepository;
import com.skillsharing.infrastructure.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

// patron observer - implementacion concreta (semana 5 - patrones de comportamiento)
// cuando una sesion cambia de estado, este observer notifica a todos los inscritos
//
// polimorfismo (poo semana 1): este componente implementa SesionObserver
// el facade no sabe que es NotificacionObserver, solo conoce la interfaz SesionObserver
//
// principio dip (semana 2): las dependencias se inyectan por constructor (no new)
// principio srp (semana 2): esta clase solo se encarga de crear notificaciones
@Component
@RequiredArgsConstructor
public class NotificacionObserver implements SesionObserver {

    private final InscripcionRepository inscripcionRepository;
    private final NotificacionRepository notificacionRepository;

    // se llama automaticamente cuando la sesion cambia de estado
    // el facade lo invoca, este observer no necesita saber quien lo llamo
    @Override
    public void onSesionActualizada(SesionAprendizaje sesion, String evento) {
        List<Inscripcion> inscritos = inscripcionRepository.findBySesionSesionId(sesion.getSesionId());

        // el mensaje varia segun el tipo de evento - expresion switch de java 14+
        String msg = switch (evento) {
            case "APROBADA"    -> "la sesion '" + sesion.getTitulo() + "' fue aprobada y ya esta disponible";
            case "RECHAZADA"   -> "la sesion '" + sesion.getTitulo() + "' fue rechazada por el administrador";
            case "INSCRIPCION" -> "nuevo participante se inscribio a '" + sesion.getTitulo() + "'";
            case "FINALIZADA"  -> "la sesion '" + sesion.getTitulo() + "' ha finalizado";
            default            -> "actualizacion en la sesion '" + sesion.getTitulo() + "'";
        };

        // crea una notificacion para cada inscrito en la sesion
        for (Inscripcion inscripcion : inscritos) {
            Notificacion notificacion = Notificacion.builder()
                    .usuario(inscripcion.getUsuario())
                    .sesion(sesion)
                    .mensaje(msg)
                    .visto(false)
                    .build();
            notificacionRepository.save(notificacion);
        }
    }
}
