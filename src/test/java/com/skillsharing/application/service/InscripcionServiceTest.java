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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// pruebas unitarias del InscripcionService - US18 y US25
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

    // US25: inscripcion con cruce de horario debe lanzar excepcion
    @Test
    void inscribir_conCruceDeHorario_debeLanzarExcepcion() {
        when(sesionRepository.findById(10L)).thenReturn(Optional.of(sesionActiva));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(aprendiz));
        when(inscripcionRepository.countActivasByUsuario(eq(2L), anyList(), any())).thenReturn(0L);
        when(inscripcionRepository.existsBySesionSesionIdAndUsuarioUsuarioId(10L, 2L)).thenReturn(false);
        when(inscripcionRepository.countBySesionId(10L)).thenReturn(5L);

        // simular cruce de horario existente
        Inscripcion conflicto = new Inscripcion();
        when(inscripcionRepository.findConflictoHorario(eq(2L), any(), any()))
                .thenReturn(List.of(conflicto));

        assertThrows(IllegalStateException.class, () ->
                inscripcionService.inscribir(10L, 2L)
        );
    }

    // US25: inscripcion sin cruce de horario debe proceder correctamente
    @Test
    void inscribir_sinCruceDeHorario_debeGuardar() {
        Inscripcion nueva = new Inscripcion();
        nueva.setSesion(sesionActiva);
        nueva.setUsuario(aprendiz);
        nueva.setRolSesion("APRENDIZ");

        when(sesionRepository.findById(10L)).thenReturn(Optional.of(sesionActiva));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(aprendiz));
        when(inscripcionRepository.countActivasByUsuario(eq(2L), anyList(), any())).thenReturn(0L);
        when(inscripcionRepository.existsBySesionSesionIdAndUsuarioUsuarioId(10L, 2L)).thenReturn(false);
        when(inscripcionRepository.countBySesionId(10L)).thenReturn(5L);
        when(inscripcionRepository.findConflictoHorario(eq(2L), any(), any())).thenReturn(List.of());
        when(inscripcionRepository.save(any(Inscripcion.class))).thenReturn(nueva);

        Inscripcion resultado = inscripcionService.inscribir(10L, 2L);

        assertNotNull(resultado);
        assertEquals("APRENDIZ", resultado.getRolSesion());
        verify(inscripcionRepository, times(1)).save(any());
    }

    // el instructor no puede inscribirse a su propia sesion
    @Test
    void inscribir_propiaSesion_debeLanzarExcepcion() {
        when(sesionRepository.findById(10L)).thenReturn(Optional.of(sesionActiva));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(instructor));

        assertThrows(IllegalStateException.class, () ->
                inscripcionService.inscribir(10L, 1L)
        );
    }
}
