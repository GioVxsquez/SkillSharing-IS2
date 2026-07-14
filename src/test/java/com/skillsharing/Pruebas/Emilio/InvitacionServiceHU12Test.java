package com.skillsharing.Pruebas.Emilio;

import com.skillsharing.application.service.*;
import com.skillsharing.domain.entity.*;
import com.skillsharing.domain.enums.*;
import com.skillsharing.infrastructure.repository.*;
import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InvitacionServiceHU12Test {
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
        invitacionService = new InvitacionService(invitacionRepository,sesionRepository,usuarioRepository,inscripcionService,notificacionService);
    }

    private Usuario instructor(){ Usuario u=new Usuario(); u.setUsuarioId(1L); return u; }
    private Usuario invitado(){ Usuario u=new Usuario(); u.setUsuarioId(2L); return u; }
    private SesionAprendizaje sesion(){
        SesionAprendizaje s=new SesionAprendizaje();
        s.setSesionId(10L); s.setInstructor(instructor());
        s.setTipo(TipoSesion.PRIVADA);
        s.setFechaSesion(LocalDateTime.now().plusDays(1));
        s.setTitulo("Java");
        return s;
    }

    @Test
    void envioExitosoInvitacion(){
        var s=sesion(); var u=invitado();
        when(sesionRepository.findById(10L)).thenReturn(Optional.of(s));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(u));
        when(invitacionRepository.existsBySesionSesionIdAndInvitadoUsuarioId(10L,2L)).thenReturn(false);
        when(invitacionRepository.countBySesionSesionIdAndEstadoIn(eq(10L),anyList())).thenReturn(0L);
        when(invitacionRepository.save(any())).thenAnswer(i->i.getArgument(0));
        assertNotNull(invitacionService.enviarInvitacion(1L,10L,2L));
        verify(invitacionRepository).save(any());
        verify(notificacionService).notificarNuevaInvitacion(u,s);
    }

    @Test
    void sesionNoEsPrivada(){
        var s=sesion(); s.setTipo(TipoSesion.PUBLICA);
        when(sesionRepository.findById(10L)).thenReturn(Optional.of(s));
        assertThrows(IllegalStateException.class,()->invitacionService.enviarInvitacion(1L,10L,2L));
    }

    @Test
    void invitacionDuplicada(){
        var s=sesion(); var u=invitado();
        when(sesionRepository.findById(10L)).thenReturn(Optional.of(s));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(u));
        when(invitacionRepository.existsBySesionSesionIdAndInvitadoUsuarioId(10L,2L)).thenReturn(true);
        assertThrows(IllegalStateException.class,()->invitacionService.enviarInvitacion(1L,10L,2L));
    }

    @Test
    void limiteInvitacionesAlcanzado(){
        var s=sesion(); var u=invitado();
        when(sesionRepository.findById(10L)).thenReturn(Optional.of(s));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(u));
        when(invitacionRepository.existsBySesionSesionIdAndInvitadoUsuarioId(10L,2L)).thenReturn(false);
        when(invitacionRepository.countBySesionSesionIdAndEstadoIn(eq(10L),anyList())).thenReturn(50L);
        assertThrows(IllegalStateException.class,()->invitacionService.enviarInvitacion(1L,10L,2L));
    }
}


/*
=========================================================
HU12 - NOTIFICAR AL INVITADO CUANDO RECIBE UNA INVITACIÓN
TÉCNICA: PARTICIÓN DE EQUIVALENCIA
=========================================================

TABLA DE CLASES DE EQUIVALENCIA

| Dato/Condición           | Clases válidas                              | Clases no válidas                                         |
|--------------------------|---------------------------------------------|-----------------------------------------------------------|
| Sesión                   | (1) Existe y organizador es su instructor   | (7) Sesión no existe  (8) Organizador no es el instructor |
| Tipo de sesión           | (2) PRIVADA                                 | (9) No es PRIVADA (PUBLICA)                               |
| Fecha de sesión          | (3) Futura (no ha iniciado)                 | (10) Ya inició                                            |
| Invitado                 | (4) Existe y es distinto al organizador     | (11) No existe  (12) Es el mismo organizador              |
| Invitación previa        | (5) No ha sido invitado antes               | (13) Ya fue invitado (RN11)                               |
| Cupo de invitaciones     | (6) Invitaciones activas =< 50              | (14) Invitaciones activas < 50 (RN12)                     |

CASOS DE PRUEBA VÁLIDOS

| Código | Tipo de sesión | Fecha de sesión  | Organizador      | Invitado                                | Clases Cubiertas              |
|--------|-----------------|-----------------|------------------|-----------------------------------------|-------------------------------|
| 300    | PRIVADA         | Futura          | Es el instructor | Existe, distinto, sin invitación previa | (1)ᶜ (2)ᶜ (3)ᶜ (4)ᶜ (5)ᶜ (6)ᶜ |

CASOS DE PRUEBA NO VÁLIDOS

| Código | Tipo de sesión  | Fecha de sesión    | Organizador              | Invitado                              |   Clases Cubiertas  |
|--------|-----------------|-------------------|---------------------------|---------------------------------------|---------------------|
| 400    | ---             | ---               | Cualquiera                | Cualquiera                            | (7)ᶜ                |
| 500    | PRIVADA         | Futura            | No es el instructor       | Válido                                | (1) (2) (3) (8)ᶜ    |
| 600    | PUBLICA         | Futura            | Es el instructor          | Válido                                | (1) (3) (9)ᶜ        |
| 700    | PRIVADA         | Ya inició         | Es el instructor          | Válido                                | (1) (2) (10)ᶜ       |
| 800    | PRIVADA         | Futura            | Es el instructor          | No existe                             | (1) (2) (3) (11)ᶜ   |
| 900    | PRIVADA         | Futura            | Es el instructor          | Mismo usuario que el organizador      | (1) (2) (3) (12)ᶜ   |
| 1000   | PRIVADA         | Futura            | Es el instructor          | Ya fue invitado antes                 | (1) (2) (3) (4) (13)ᶜ |
| 1100   | PRIVADA         | Futura            | Es el instructor          | Válido, con 51 invitaciones activas   | (1) (2) (3) (4) (5) (14)ᶜ |
*/


/* =========================================================
   HISTORIA DE USUARIO HU12 - NOTIFICAR AL INVITADO
   PRUEBA DE CAJA BLANCA
   TÉCNICA: PRUEBA DEL CAMINO BÁSICO
   MÉTODO: InvitacionService.enviarInvitacion()
   =========================================================

   GRAFO DE FLUJO

                              (1)
                               |
                               v
                    ¿Sesión existe?
                    /                \
                  No                  Sí
                   |                   |
                   v                   v
             Retorna error           (2)
        "sesion no encontrada"   ¿Organizador es
                   |             instructor de la sesión?
                   |              /            \
                   |            No              Sí
                   |             |                |
                   |             v                v
                   |       Retorna error         (3)
                   |    "solo el organizador   ¿Tipo de sesión
                   |     puede enviar           == PRIVADA?
                   |     invitaciones"           /        \
                   |             |             No          Sí
                   |             |              |            |
                   |             |              v            v
                   |             |        Retorna error     (4)
                   |             |    "solo las sesiones  ¿Fecha sesión
                   |             |     privadas usan       ya pasó?
                   |             |     invitaciones"       /      \
                   |             |              |         Sí       No
                   |             |              |          |        |
                   |             |              |          v        v
                   |             |              |    Retorna error (5)
                   |             |              |  "no se puede   ¿Invitado
                   |             |              |   invitar..."   existe?
                   |             |              |          |      /     \
                   |             |              |          |    No       Sí
                   |             |              |          |     |        |
                   |             |              |          |     v        v
                   |             |              |          | Retorna    (6)
                   |             |              |          | error     ¿Invitado ==
                   |             |              |          |"invitado   organizador?
                   |             |              |          | no          /      \
                   |             |              |          |encontrado" Sí       No
                   |             |              |          |    |        |        |
                   |             |              |          |    |        v        v
                   |             |              |          |    |   Retorna     (7)
                   |             |              |          |    |   error    ¿Ya fue
                   |             |              |          |    |  "no puedes  invitado?
                   |             |              |          |    |  invitarte..." /    \
                   |             |              |          |    |       |      Sí      No
                   |             |              |          |    |       |       |       |
                   |             |              |          |    |       |       v       v
                   |             |              |          |    |       |   Retorna   (8)
                   |             |              |          |    |       |   error   ¿Invitaciones
                   |             |              |          |    |       |  "ya ha    activas >= 50?
                   |             |              |          |    |       |   sido       /      \
                   |             |              |          |    |       |  invitado"  Sí       No
                   |             |              |          |    |       |     |        |        |
                   |             |              |          |    |       |     |        v        v
                   |             |              |          |    |       |     |    Retorna    (9)
                   |             |              |          |    |       |     |    error    Crear invitación,
                   |             |              |          |    |       |     |   "límite    guardar y
                   |             |              |          |    |       |     |   máximo"    NOTIFICAR
                   |             |              |          |    |       |     |     |        AL INVITADO
                    \            \              \          \    \       \     \     |            |
                     \            \              \          \    \       \     \     v            v
                      \____________\______________\__________\____\_______\_____\___(10)_________/
                                                                                      FIN

   COMPLEJIDAD CICLOMÁTICA

   Número de decisiones:
   1. ¿Sesión existe?
   2. ¿Organizador es instructor de la sesión?
   3. ¿Tipo de sesión == PRIVADA?
   4. ¿Fecha sesión ya pasó?
   5. ¿Invitado existe?
   6. ¿Invitado == organizador?
   7. ¿Ya fue invitado?
   8. ¿Invitaciones activas >= 50?

   V(G) = Número de decisiones + 1
   V(G) = 8 + 1 = 9

   Se requieren 9 caminos independientes.

   CAMINOS INDEPENDIENTES

   Camino 1: 1(No)              -> FIN (error: sesión no encontrada)
   Camino 2: 1(Sí),2(No)        -> FIN (error: no autorizado)
   Camino 3: 1(Sí),2(Sí),3(No)  -> FIN (error: sesión no privada)
   Camino 4: ...3(Sí),4(Sí)     -> FIN (error: sesión ya inició)
   Camino 5: ...4(No),5(No)     -> FIN (error: invitado no encontrado)
   Camino 6: ...5(Sí),6(Sí)     -> FIN (error: auto-invitación)
   Camino 7: ...6(No),7(Sí)     -> FIN (error: ya invitado - RN11)
   Camino 8: ...7(No),8(Sí)     -> FIN (error: límite 50 - RN12)
   Camino 9: ...8(No)           -> FIN (éxito: se crea invitación y SE NOTIFICA al invitado)

   CASOS DE PRUEBA

   | Camino   | Datos de prueba                                                                 | Resultado esperado                                                                  |
   |----------|----------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|
   | Camino 1 | sesionId no existe en el repositorio.                                           | RuntimeException "sesion no encontrada". notificacionService NO es invocado.        |
   | Camino 2 | organizadorId distinto al instructor de la sesión.                             | IllegalStateException "solo el organizador...". notificacionService NO es invocado. |
   | Camino 3 | Sesión con tipo != PRIVADA (ej. PUBLICA).                                       | IllegalStateException "solo las sesiones privadas...". Sin notificación.           |
   | Camino 4 | Sesión privada con fechaSesion anterior a la fecha actual.                     | IllegalStateException "no se puede invitar...". Sin notificación.                   |
   | Camino 5 | invitadoId no existe en el repositorio.                                         | RuntimeException "invitado no encontrado". Sin notificación.                        |
   | Camino 6 | invitadoId == organizadorId.                                                    | IllegalStateException "no puedes invitarte...". Sin notificación.                    |
   | Camino 7 | Ya existe una invitación previa para ese usuario en esa sesión.                | IllegalStateException "ya ha sido invitado (RN11)". Sin notificación.               |
   | Camino 8 | Sesión con 50 invitaciones en estado PENDIENTE o ACEPTADA.                     | IllegalStateException "límite máximo de 50 invitaciones (RN12)". Sin notificación.  |
   | Camino 9 | Sesión privada, futura, organizador válido, invitado válido, sin invitación previa, <50 invitaciones activas. | Se guarda la invitación (estado PENDIENTE) y notificacionService.notificarNuevaInvitacion(invitado, sesion) ES invocado exactamente una vez. |

   ========================================================= */