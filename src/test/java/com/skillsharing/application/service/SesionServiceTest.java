package com.skillsharing.application.service;

import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.domain.enums.TipoSesion;
import com.skillsharing.infrastructure.repository.SesionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SesionServiceTest {

    @Mock
    private SesionRepository sesionRepository;

    @InjectMocks
    private SesionService sesionService;

    private SesionAprendizaje sesionActiva;
    private Usuario instructor;

    @BeforeEach
    void setUp() {
        instructor = new Usuario();
        instructor.setUsuarioId(1L);
        instructor.setNombre("Instructor Test");

        sesionActiva = new SesionAprendizaje();
        sesionActiva.setSesionId(5L);
        sesionActiva.setTitulo("Sesion de Java");
        sesionActiva.setEstado(EstadoSesion.ACTIVA);
        sesionActiva.setTipo(TipoSesion.PUBLICA);
        sesionActiva.setFechaSesion(LocalDateTime.now().plusDays(2));
        sesionActiva.setInstructor(instructor);
    }

    // US08: CANCELAR O ELIMINAR SESIÓN DE APRENDIZAJE

    // Rama 1: Camino Feliz - Cancelación Exitosa
    @Test
    void cancelarSesion_comoInstructor_debeCambiarEstadoYGuardar() {
        when(sesionRepository.findById(5L)).thenReturn(Optional.of(sesionActiva));
        when(sesionRepository.save(any(SesionAprendizaje.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SesionAprendizaje resultado = sesionService.cancelarSesion(5L, 1L);

        assertNotNull(resultado);
        assertEquals(EstadoSesion.CANCELADA, resultado.getEstado());
        verify(sesionRepository, times(1)).save(sesionActiva);
    }

    // Rama 2: Excepción - Sesión No Encontrada
    @Test
    void cancelarSesion_CuandoNoExiste_LanzaRuntimeException() {
        when(sesionRepository.findById(5L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            sesionService.cancelarSesion(5L, 1L);
        });

        assertEquals("sesion no encontrada", exception.getMessage());
        verify(sesionRepository, never()).save(any());
    }

    // Rama 3: Excepción - Control de Seguridad (Otro usuario no puede cancelar)
    @Test
    void cancelarSesion_sinPermiso_debeLanzarSecurityException() {
        when(sesionRepository.findById(5L)).thenReturn(Optional.of(sesionActiva));

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            sesionService.cancelarSesion(5L, 99L);
        });

        assertEquals("solo el instructor dueno puede cancelar la sesion", exception.getMessage());
        verify(sesionRepository, never()).save(any());
    }

    // Rama 4: Excepción - Estado Inválido (Ya cancelada)
    @Test
    void cancelarSesion_yaEstaCancelada_debeLanzarIllegalStateException() {
        sesionActiva.setEstado(EstadoSesion.CANCELADA);
        when(sesionRepository.findById(5L)).thenReturn(Optional.of(sesionActiva));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            sesionService.cancelarSesion(5L, 1L);
        });

        assertEquals("no se puede cancelar una sesion ya finalizada o cancelada", exception.getMessage());
        verify(sesionRepository, never()).save(any());
    }

    // Rama 5: Excepción - Estado Inválido (Ya finalizada)
    @Test
    void cancelarSesion_yaEstaFinalizada_debeLanzarIllegalStateException() {
        sesionActiva.setEstado(EstadoSesion.FINALIZADA);
        when(sesionRepository.findById(5L)).thenReturn(Optional.of(sesionActiva));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            sesionService.cancelarSesion(5L, 1L);
        });

        assertEquals("no se puede cancelar una sesion ya finalizada o cancelada", exception.getMessage());
        verify(sesionRepository, never()).save(any());
    }
}