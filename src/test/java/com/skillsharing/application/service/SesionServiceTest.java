package com.skillsharing.application.service;

import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.domain.enums.TipoSesion;
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

// pruebas unitarias del SesionService - US03 y US08
@ExtendWith(MockitoExtension.class)
class SesionServiceTest {

    @Mock
    private SesionRepository sesionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

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

    // US08: cancelar sesion activa como su instructor debe cambiar estado a CANCELADA
    @Test
    void cancelarSesion_comoInstructor_debeCambiarEstado() {
        when(sesionRepository.findById(5L)).thenReturn(Optional.of(sesionActiva));
        when(sesionRepository.save(any(SesionAprendizaje.class))).thenReturn(sesionActiva);

        SesionAprendizaje resultado = sesionService.cancelarSesion(5L, 1L);

        assertEquals(EstadoSesion.CANCELADA, resultado.getEstado());
        verify(sesionRepository, times(1)).save(sesionActiva);
    }

    // US08: otro usuario que no es el instructor no puede cancelar
    @Test
    void cancelarSesion_sinPermiso_debeLanzarExcepcion() {
        when(sesionRepository.findById(5L)).thenReturn(Optional.of(sesionActiva));

        assertThrows(SecurityException.class, () ->
                sesionService.cancelarSesion(5L, 99L)
        );

        verify(sesionRepository, never()).save(any());
    }

    // US08: no se puede cancelar una sesion ya cancelada
    @Test
    void cancelarSesion_yaEstaCancelada_debeLanzarExcepcion() {
        sesionActiva.setEstado(EstadoSesion.CANCELADA);
        when(sesionRepository.findById(5L)).thenReturn(Optional.of(sesionActiva));

        assertThrows(IllegalStateException.class, () ->
                sesionService.cancelarSesion(5L, 1L)
        );
    }

    // US08: no se puede cancelar una sesion finalizada
    @Test
    void cancelarSesion_yaEstaFinalizada_debeLanzarExcepcion() {
        sesionActiva.setEstado(EstadoSesion.FINALIZADA);
        when(sesionRepository.findById(5L)).thenReturn(Optional.of(sesionActiva));

        assertThrows(IllegalStateException.class, () ->
                sesionService.cancelarSesion(5L, 1L)
        );
    }

    // US03: buscar sesiones por nombre debe delegar al repositorio
    @Test
    void buscarPorNombre_debeRetornarCoincidencias() {
        when(sesionRepository.findByTituloContainingIgnoreCaseAndEstadoAndTipo(
                "java", EstadoSesion.ACTIVA, TipoSesion.PUBLICA))
                .thenReturn(List.of(sesionActiva));

        List<SesionAprendizaje> resultado = sesionService.buscarPorNombre("java");

        assertEquals(1, resultado.size());
        assertEquals("Sesion de Java", resultado.get(0).getTitulo());
    }

    // US03: busqueda sin resultados debe retornar lista vacia
    @Test
    void buscarPorNombre_sinResultados_debeRetornarListaVacia() {
        when(sesionRepository.findByTituloContainingIgnoreCaseAndEstadoAndTipo(
                "xyz", EstadoSesion.ACTIVA, TipoSesion.PUBLICA))
                .thenReturn(List.of());

        List<SesionAprendizaje> resultado = sesionService.buscarPorNombre("xyz");

        assertTrue(resultado.isEmpty());
    }

    // obtener detalle de sesion existente debe retornarla
    @Test
    void obtenerDetalle_sesionExistente_debeRetornar() {
        when(sesionRepository.findById(5L)).thenReturn(Optional.of(sesionActiva));

        SesionAprendizaje s = sesionService.obtenerDetalle(5L);

        assertNotNull(s);
        assertEquals(5L, s.getSesionId());
    }

    // obtener detalle de sesion inexistente debe lanzar excepcion
    @Test
    void obtenerDetalle_sesionInexistente_debeLanzarExcepcion() {
        when(sesionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                sesionService.obtenerDetalle(99L)
        );
    }

    // US09: filtrar por categoria delega al repositorio
    @Test
    void filtrarPorCategoria_debeDelegarARepository() {
        when(sesionRepository.findByCategoriaIgnoreCaseAndEstadoAndTipoAndFechaSesionAfterOrderByFechaSesionAsc(
                eq("Programacion"), eq(EstadoSesion.ACTIVA), eq(TipoSesion.PUBLICA), any(LocalDateTime.class)))
                .thenReturn(List.of(sesionActiva));

        List<SesionAprendizaje> resultado = sesionService.filtrarPorCategoria("Programacion");

        assertEquals(1, resultado.size());
        assertEquals("Sesion de Java", resultado.get(0).getTitulo());
    }

    // US11: filtrar por modalidad delega al repositorio
    @Test
    void filtrarPorModalidad_debeDelegarARepository() {
        when(sesionRepository.findByModalidadAndEstadoAndTipoAndFechaSesionAfterOrderByFechaSesionAsc(
                eq(com.skillsharing.domain.enums.ModalidadSesion.VIRTUAL), eq(EstadoSesion.ACTIVA), eq(TipoSesion.PUBLICA), any(LocalDateTime.class)))
                .thenReturn(List.of(sesionActiva));

        List<SesionAprendizaje> resultado = sesionService.filtrarPorModalidad(com.skillsharing.domain.enums.ModalidadSesion.VIRTUAL);

        assertEquals(1, resultado.size());
        assertEquals(5L, resultado.get(0).getSesionId());
    }

    // US01/US25: crear sesion con capacidad VÁLIDA (valores dentro de rango y límite)
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(ints = {1, 20, 100})
    void crearSesion_capacidadValida_debeCrearSesion(int capacidad) {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(sesionRepository.existsByTituloIgnoreCase("Titulo")).thenReturn(false);
        when(sesionRepository.countByInstructorUsuarioIdAndEstadoIn(eq(1L), any())).thenReturn(0L);

        com.skillsharing.application.dto.request.SesionRequestDto dto = new com.skillsharing.application.dto.request.SesionRequestDto();
        dto.setTitulo("Titulo");
        dto.setDescripcion("Desc");
        dto.setFechaSesion(LocalDateTime.now().plusDays(1));
        dto.setModalidad("VIRTUAL");
        dto.setMaxParticipantes(capacidad);
        dto.setLinkSesion("link");

        SesionAprendizaje creada = new SesionAprendizaje();
        creada.setSesionId(10L);
        when(sesionRepository.save(any())).thenReturn(creada);

        SesionAprendizaje resultado = sesionService.crearSesion(1L, dto);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getSesionId());
        verify(sesionRepository).save(any());
    }

    // US01/US25: crear sesion con capacidad INVÁLIDA (fuera de los valores límite)
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(ints = {-5, 0, 101})
    void crearSesion_capacidadInvalida_debeLanzarExcepcion(int capacidadInvalida) {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(sesionRepository.existsByTituloIgnoreCase("Titulo")).thenReturn(false);
        when(sesionRepository.countByInstructorUsuarioIdAndEstadoIn(eq(1L), any())).thenReturn(0L);

        com.skillsharing.application.dto.request.SesionRequestDto dto = new com.skillsharing.application.dto.request.SesionRequestDto();
        dto.setTitulo("Titulo");
        dto.setMaxParticipantes(capacidadInvalida);

        assertThrows(IllegalArgumentException.class, () -> 
            sesionService.crearSesion(1L, dto)
        );
        verify(sesionRepository, never()).save(any());
    }
}
