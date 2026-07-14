package com.skillsharing.Pruebas.Emilio;

import com.skillsharing.application.service.*;
import com.skillsharing.domain.entity.*;
import com.skillsharing.domain.enums.*;
import com.skillsharing.infrastructure.repository.*;
import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvitacionServiceHU13Test {

    private InvitacionRepository invitacionRepository;
    private SesionRepository sesionRepository;
    private UsuarioRepository usuarioRepository;
    private InscripcionService inscripcionService;
    private NotificacionService notificacionService;
    private InvitacionService invitacionService;

    @BeforeEach
    void setUp() {
        invitacionRepository = mock(InvitacionRepository.class);
        sesionRepository = mock(SesionRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        inscripcionService = mock(InscripcionService.class);
        notificacionService = mock(NotificacionService.class);

        invitacionService = new InvitacionService(
                invitacionRepository,
                sesionRepository,
                usuarioRepository,
                inscripcionService,
                notificacionService
        );
    }

    private Usuario invitado() {
        Usuario u = new Usuario();
        u.setUsuarioId(2L);
        return u;
    }

    private Usuario instructor() {
        Usuario u = new Usuario();
        u.setUsuarioId(1L);
        return u;
    }

    private Invitacion invitacion() {
        SesionAprendizaje s = new SesionAprendizaje();
        s.setSesionId(10L);
        s.setInstructor(instructor());
        s.setFechaSesion(LocalDateTime.now().plusDays(1));
        Invitacion i = new Invitacion();
        i.setInvitacionId(5L);
        i.setInvitado(invitado());
        i.setSesion(s);
        i.setEstado(EstadoInvitacion.PENDIENTE);
        i.setFechaEnvio(LocalDateTime.now());
        return i;
    }

    @Test
    void aceptarInvitacion() {
        Invitacion inv = invitacion();
        when(invitacionRepository.findById(5L)).thenReturn(Optional.of(inv));

        invitacionService.responderInvitacion(2L,5L,true);

        assertEquals(EstadoInvitacion.ACEPTADA, inv.getEstado());
        verify(inscripcionService).inscribir(10L,2L);
        verify(notificacionService).notificarRespuestaInvitacion(inv);
    }

    @Test
    void rechazarInvitacion() {
        Invitacion inv = invitacion();
        when(invitacionRepository.findById(5L)).thenReturn(Optional.of(inv));

        invitacionService.responderInvitacion(2L,5L,false);

        assertEquals(EstadoInvitacion.RECHAZADA, inv.getEstado());
        verify(inscripcionService, never()).inscribir(anyLong(), anyLong());
        verify(notificacionService).notificarRespuestaInvitacion(inv);
    }

    @Test
    void invitacionYaRespondida() {
        Invitacion inv = invitacion();
        inv.setEstado(EstadoInvitacion.ACEPTADA);
        when(invitacionRepository.findById(5L)).thenReturn(Optional.of(inv));

        assertThrows(IllegalStateException.class,
                () -> invitacionService.responderInvitacion(2L,5L,true));

        verify(notificacionService, never()).notificarRespuestaInvitacion(any());
    }

    @Test
    void invitacionExpirada() {
        Invitacion inv = invitacion();
        inv.setFechaEnvio(LocalDateTime.now().minusDays(6));
        when(invitacionRepository.findById(5L)).thenReturn(Optional.of(inv));

        assertThrows(IllegalStateException.class,
                () -> invitacionService.responderInvitacion(2L,5L,true));

        verify(notificacionService, never()).notificarRespuestaInvitacion(any());
    }
}



/*
=========================================================
HU13 - NOTIFICAR AL ORGANIZADOR CUANDO EL INVITADO RESPONDE
TÉCNICA: PARTICIÓN DE EQUIVALENCIA
=========================================================

TABLA DE CLASES DE EQUIVALENCIA

| Dato/Condición        | Clases válidas                               | Clases no válidas                                |
|------------------------|--------------------------------------------------|------------------------------------------------------|
| Invitación             | (1) Existe                                        | (8) No existe                                          |
| Autorización           | (2) Quien responde es el invitado correcto        | (9) Quien responde no es el invitado                   |
| Estado de invitación   | (3) PENDIENTE                                     | (10) Ya fue respondida (ACEPTADA/RECHAZADA)             |
| Vigencia               | (4) Dentro de 5 días desde el envío               | (11) Expiró (>5 días) (RN17)                            |
| Estado de sesión       | (5) Sesión aún no ha iniciado                     | (12) Sesión ya inició                                   |
| Tipo de respuesta      | (6) Aceptar = true   (7) Rechazar = false         | ---                                                     |

CASOS DE PRUEBA VÁLIDOS

| Código | Estado invitación | Vigencia          | Autorización       | Estado sesión     | Respuesta      | Clases Cubiertas               |
|--------|----------------------|----------------------|-----------------------|-----------------------|-------------------|-------------------------------------|
| 300    | PENDIENTE            | Dentro de 5 días     | Invitado correcto      | No ha iniciado        | Aceptar (true)     | (1)ᶜ (2)ᶜ (3)ᶜ (4)ᶜ (5)ᶜ (6)ᶜ         |
| 400    | PENDIENTE            | Dentro de 5 días     | Invitado correcto      | No ha iniciado        | Rechazar (false)   | (1) (2) (3) (4) (5) (7)ᶜ             |

CASOS DE PRUEBA NO VÁLIDOS

| Código | Estado invitación   | Vigencia         | Autorización              | Estado sesión      | Respuesta   | Clases Cubiertas |
|--------|-------------------------|---------------------|-------------------------------|-------------------------|----------------|----------------------|
| 500    | ---                     | ---                 | Cualquiera                    | ---                     | true/false     | (8)ᶜ                |
| 600    | PENDIENTE               | Dentro de 5 días    | Usuario distinto al invitado  | No ha iniciado          | true/false     | (1) (3) (4) (5) (9)ᶜ |
| 700    | Ya respondida (ACEPTADA)| ---                 | Invitado correcto             | No ha iniciado          | true/false     | (1) (2) (5) (10)ᶜ   |
| 800    | PENDIENTE               | Enviada hace 6 días | Invitado correcto             | No ha iniciado          | true/false     | (1) (2) (3) (5) (11)ᶜ |
| 900    | PENDIENTE               | Dentro de 5 días    | Invitado correcto             | Ya inició               | true/false     | (1) (2) (3) (4) (12)ᶜ |
*/




/* =========================================================
   HISTORIA DE USUARIO HU13 - NOTIFICAR AL ORGANIZADOR
   PRUEBA DE CAJA BLANCA
   TÉCNICA: PRUEBA DEL CAMINO BÁSICO
   MÉTODO: InvitacionService.responderInvitacion()
   =========================================================

   GRAFO DE FLUJO

                              (1)
                               |
                               v
                  ¿Invitación existe?
                    /                \
                  No                  Sí
                   |                   |
                   v                   v
             Retorna error            (2)
        "invitacion no             ¿Invitado != quien
         encontrada"                responde?
                   |               /            \
                   |             Sí              No
                   |              |                |
                   |              v                v
                   |        Retorna error         (3)
                   |     "no tienes permiso     ¿Estado !=
                   |      para responder..."     PENDIENTE?
                   |              |               /        \
                   |              |             Sí          No
                   |              |              |            |
                   |              |              v            v
                   |              |        Retorna error     (4)
                   |              |     "la invitacion ya  ¿Expiró (fechaEnvio
                   |              |      fue respondida"    + 5 días < ahora)?
                   |              |              |            /        \
                   |              |              |          Sí          No
                   |              |              |           |            |
                   |              |              |           v            v
                   |              |              |     Retorna error     (5)
                   |              |              |   "ha expirado      ¿Sesión ya
                   |              |              |    (RN17)"           inició?
                   |              |              |           |          /      \
                   |              |              |           |        Sí       No
                   |              |              |           |         |         |
                   |              |              |           |         v         v
                   |              |              |           |    Retorna     (6)
                   |              |              |           |    error    ¿aceptar == true?
                   |              |              |           |  "sesion ya    /        \
                   |              |              |           |   inició..."  Sí         No
                   |              |              |           |       |       |           |
                   |              |              |           |       |       v           v
                   |              |              |           |       |  Estado=       Estado=
                   |              |              |           |       |  ACEPTADA,      RECHAZADA
                   |              |              |           |       |  inscribir()       |
                   |              |              |           |       |       \           /
                   |              |              |           |       |        \         /
                   |              |              |           |       |         v       v
                   |              |              |           |       |          (7)
                   |              |              |           |       |    Guardar y
                   |              |              |           |       |    NOTIFICAR AL
                   |              |              |           |       |    ORGANIZADOR
                    \              \              \           \       \        |
                     \              \              \           \       \       v
                      \______________\______________\___________\_______\____(8)
                                                                            FIN

   COMPLEJIDAD CICLOMÁTICA

   Número de decisiones:
   1. ¿Invitación existe?
   2. ¿Invitado != quien responde?
   3. ¿Estado != PENDIENTE?
   4. ¿Expiró (5 días)?
   5. ¿Sesión ya inició?
   6. ¿aceptar == true?

   V(G) = Número de decisiones + 1
   V(G) = 6 + 1 = 7

   Se requieren 7 caminos independientes.

   CAMINOS INDEPENDIENTES

   Camino 1: 1(No)                     -> FIN (error: invitación no encontrada)
   Camino 2: 1(Sí),2(Sí)               -> FIN (error: sin permiso)
   Camino 3: 1(Sí),2(No),3(Sí)         -> FIN (error: ya respondida)
   Camino 4: ...3(No),4(Sí)            -> FIN (error: expirada - RN17)
   Camino 5: ...4(No),5(Sí)            -> FIN (error: sesión ya inició)
   Camino 6: ...5(No),6(Sí, aceptar)   -> FIN (éxito: ACEPTADA, inscribe y SE NOTIFICA al organizador)
   Camino 7: ...5(No),6(No, rechazar)  -> FIN (éxito: RECHAZADA y SE NOTIFICA al organizador)

   CASOS DE PRUEBA

   | Camino   | Datos de prueba                                                                        | Resultado esperado                                                                                    |
   |----------|-------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
   | Camino 1 | invitacionId no existe en el repositorio.                                                | RuntimeException "invitacion no encontrada". notificacionService NO es invocado.                       |
   | Camino 2 | invitadoId del request distinto al invitado real de la invitación.                       | IllegalStateException "no tienes permiso...". Sin notificación.                                        |
   | Camino 3 | Invitación con estado ya ACEPTADA o RECHAZADA.                                           | IllegalStateException "la invitacion ya fue respondida". Sin notificación.                             |
   | Camino 4 | Invitación con fechaEnvio de hace más de 5 días, estado PENDIENTE.                      | IllegalStateException "ha expirado despues de 5 dias (RN17)". Sin notificación.                        |
   | Camino 5 | Invitación PENDIENTE, dentro de plazo, pero sesion.fechaSesion ya pasó.                 | IllegalStateException "la sesion ya inicio...". Sin notificación.                                      |
   | Camino 6 | Invitación PENDIENTE, dentro de plazo, sesión futura, aceptar=true.                     | Estado pasa a ACEPTADA, inscripcionService.inscribir() se invoca, y notificacionService.notificarRespuestaInvitacion(inv) ES invocado. |
   | Camino 7 | Invitación PENDIENTE, dentro de plazo, sesión futura, aceptar=false.                    | Estado pasa a RECHAZADA (no se invoca inscribir), y notificacionService.notificarRespuestaInvitacion(inv) ES invocado. |

   ========================================================= */

