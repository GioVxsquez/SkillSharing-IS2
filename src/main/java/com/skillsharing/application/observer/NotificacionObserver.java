package com.skillsharing.application.observer;
import com.skillsharing.domain.entity.Inscripcion;
import com.skillsharing.domain.entity.Notificacion;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.infrastructure.repository.InscripcionRepository;
import com.skillsharing.infrastructure.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
@Component
@RequiredArgsConstructor
public class NotificacionObserver implements SesionObserver {
    private final InscripcionRepository inscripcionRepository;
    private final NotificacionRepository notificacionRepository;
    @Override
    public void onSesionActualizada(SesionAprendizaje sesion, String evento) {
        List<Inscripcion> inscritos = inscripcionRepository.findBySesionSesionId(sesion.getSesionId());
        String msg = switch (evento) {
            case "APROBADA"    -> "la sesion '" + sesion.getTitulo() + "' fue aprobada y ya esta disponible";
            case "RECHAZADA"   -> "la sesion '" + sesion.getTitulo() + "' fue rechazada por el administrador";
            case "INSCRIPCION" -> "nuevo participante se inscribio a '" + sesion.getTitulo() + "'";
            case "FINALIZADA"  -> "la sesion '" + sesion.getTitulo() + "' ha finalizado";
            default            -> "actualizacion en la sesion '" + sesion.getTitulo() + "'";
        };
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
