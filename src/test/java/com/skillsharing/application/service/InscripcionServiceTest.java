package com.skillsharing.application.service;

import com.skillsharing.domain.entity.Inscripcion;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.infrastructure.repository.InscripcionRepository;
import com.skillsharing.infrastructure.repository.SesionRepository;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// pruebas unitarias del InscripcionService - US18
@ExtendWith(MockitoExtension.class)
class InscripcionServiceTest {

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private SesionRepository sesionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private InscripcionService inscripcionService;

    private SesionAprendizaje sesionActiva;
    private Usuario aprendiz;
    private Usuario instructor;

    @BeforeEach
    void setUp() {
        instructor = new Usuario();
        instructor.setUsuarioId(1L);

        aprendiz = new Usuario();
        aprendiz.setUsuarioId(2L);

        sesionActiva = new SesionAprendizaje();
        sesionActiva.setSesionId(10L);
        sesionActiva.setEstado(EstadoSesion.ACTIVA);
        sesionActiva.setFechaSesion(LocalDateTime.now().plusDays(3));
        sesionActiva.setMaxParticipantes(20);
        sesionActiva.setInstructor(instructor);
    }

    // US18: desinscribirse antes del inicio debe eliminar la inscripcion
    @Test
    void desinscribirse_antesDelInicio_debeEliminar() {
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setSesion(sesionActiva);
        inscripcion.setUsuario(aprendiz);

        when(sesionRepository.findById(10L)).thenReturn(Optional.of(sesionActiva));
        when(inscripcionRepository.findBySesionSesionIdAndUsuarioUsuarioId(10L, 2L))
                .thenReturn(Optional.of(inscripcion));

        inscripcionService.desinscribirse(10L, 2L);

        verify(inscripcionRepository, times(1)).delete(inscripcion);
    }

    // US18: desinscribirse de sesion ya iniciada debe lanzar excepcion
    @Test
    void desinscribirse_sesionYaIniciada_debeLanzarExcepcion() {
        sesionActiva.setFechaSesion(LocalDateTime.now().minusHours(1));

        when(sesionRepository.findById(10L)).thenReturn(Optional.of(sesionActiva));

        assertThrows(IllegalStateException.class, () ->
                inscripcionService.desinscribirse(10L, 2L)
        );

        verify(inscripcionRepository, never()).delete(any());
    }
}

