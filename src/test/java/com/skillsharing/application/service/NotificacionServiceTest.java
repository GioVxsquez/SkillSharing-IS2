package com.skillsharing.application.service;

import com.skillsharing.domain.entity.Notificacion;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// US12, US13: Sistema de notificaciones automaticas
@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private NotificacionService notificacionService;

    private Usuario usuario;
    private SesionAprendizaje sesion;
    private Notificacion notificacion;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setUsuarioId(1L);

        sesion = new SesionAprendizaje();
        sesion.setSesionId(2L);

        notificacion = new Notificacion();
        notificacion.setNotificacionId(10L);
        notificacion.setUsuario(usuario);
        notificacion.setSesion(sesion);
        notificacion.setMensaje("Tienes una nueva invitacion");
        notificacion.setVisto(false);
    }

    // US12: Crear notificacion debe guardarla con estado visto=false
    @Test
    void crearNotificacion_debeGuardarCorrectamente() {
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(i -> i.getArguments()[0]);

        Notificacion creada = notificacionService.crear(usuario, sesion, "Mensaje de prueba");

        assertNotNull(creada);
        assertEquals(usuario, creada.getUsuario());
        assertEquals("Mensaje de prueba", creada.getMensaje());
        assertFalse(creada.getVisto());
        verify(notificacionRepository, times(1)).save(any());
    }

    // Listar notificaciones por usuario delega al repo
    @Test
    void listarPorUsuario_debeRetornarLista() {
        when(notificacionRepository.findByUsuarioUsuarioIdOrderByFechaCreacionDesc(1L))
                .thenReturn(List.of(notificacion));

        List<Notificacion> resultado = notificacionService.listarPorUsuario(1L);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getNotificacionId());
    }

    // Marcar como vista cambia el booleano
    @Test
    void marcarComoVista_debeActualizarEstado() {
        when(notificacionRepository.findById(10L)).thenReturn(Optional.of(notificacion));

        notificacionService.marcarComoVista(10L);

        assertTrue(notificacion.getVisto());
        verify(notificacionRepository, times(1)).save(notificacion);
    }

    // Marcar todas como vistas
    @Test
    void marcarTodasComoVistas_debeActualizarVarias() {
        Notificacion notif2 = new Notificacion();
        notif2.setVisto(false);

        when(notificacionRepository.findByUsuarioUsuarioIdAndVistoFalseOrderByFechaCreacionDesc(1L))
                .thenReturn(List.of(notificacion, notif2));

        notificacionService.marcarTodasComoVistas(1L);

        assertTrue(notificacion.getVisto());
        assertTrue(notif2.getVisto());
        verify(notificacionRepository, times(1)).saveAll(any());
    }
}
