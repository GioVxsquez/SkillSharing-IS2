package com.skillsharing.application.service;

import com.skillsharing.domain.entity.Calificacion;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.infrastructure.repository.CalificacionRepository;
import com.skillsharing.infrastructure.repository.InscripcionRepository;
import com.skillsharing.infrastructure.repository.SesionRepository;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// pruebas unitarias del CalificacionService - US19 y US20
// se usa Mockito para aislar el servicio de sus dependencias (repositorios)
@ExtendWith(MockitoExtension.class)
class CalificacionServiceTest {

    @Mock
    private CalificacionRepository calificacionRepository;

    @Mock
    private SesionRepository sesionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private InscripcionRepository inscripcionRepository;

    @InjectMocks
    private CalificacionService calificacionService;

    private SesionAprendizaje sesionFinalizada;
    private Usuario aprendiz;

    @BeforeEach
    void setUp() {
        aprendiz = new Usuario();
        aprendiz.setUsuarioId(10L);
        aprendiz.setNombre("Aprendiz Test");

        sesionFinalizada = new SesionAprendizaje();
        sesionFinalizada.setSesionId(1L);
        sesionFinalizada.setEstado(EstadoSesion.FINALIZADA);
    }

    // US19: calificar una sesion finalizada con datos validos debe guardarse correctamente
    @Test
    void calificar_sesionFinalizada_debeGuardarCalificacion() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesionFinalizada));
        when(inscripcionRepository.existsBySesionSesionIdAndUsuarioUsuarioId(1L, 10L)).thenReturn(true);
        when(calificacionRepository.existsBySesionSesionIdAndUsuarioUsuarioId(1L, 10L)).thenReturn(false);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(aprendiz));

        Calificacion guardada = Calificacion.builder()
                .sesion(sesionFinalizada)
                .usuario(aprendiz)
                .puntuacion(4)
                .comentario("Muy buena sesion")
                .build();

        when(calificacionRepository.save(any(Calificacion.class))).thenReturn(guardada);

        Calificacion resultado = calificacionService.calificar(1L, 10L, 4, "Muy buena sesion");

        assertNotNull(resultado);
        assertEquals(4, resultado.getPuntuacion());
        assertEquals("Muy buena sesion", resultado.getComentario());
        verify(calificacionRepository, times(1)).save(any(Calificacion.class));
    }

    // US19: calificar sesion no finalizada debe lanzar excepcion
    @Test
    void calificar_sesionNoFinalizada_debeLanzarExcepcion() {
        sesionFinalizada.setEstado(EstadoSesion.ACTIVA);
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesionFinalizada));

        assertThrows(IllegalStateException.class, () ->
                calificacionService.calificar(1L, 10L, 5, "comentario")
        );

        verify(calificacionRepository, never()).save(any());
    }

    // US19: usuario no inscrito no puede calificar
    @Test
    void calificar_usuarioNoInscrito_debeLanzarExcepcion() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesionFinalizada));
        when(inscripcionRepository.existsBySesionSesionIdAndUsuarioUsuarioId(1L, 10L)).thenReturn(false);

        assertThrows(IllegalStateException.class, () ->
                calificacionService.calificar(1L, 10L, 4, "")
        );
    }

    // US19: puntuacion fuera de rango debe lanzar excepcion
    @Test
    void calificar_puntuacionFueraDeRango_debeLanzarExcepcion() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesionFinalizada));
        when(inscripcionRepository.existsBySesionSesionIdAndUsuarioUsuarioId(1L, 10L)).thenReturn(true);
        when(calificacionRepository.existsBySesionSesionIdAndUsuarioUsuarioId(1L, 10L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                calificacionService.calificar(1L, 10L, 6, "")
        );
    }

    // US19: no se puede calificar dos veces la misma sesion
    @Test
    void calificar_duplicado_debeLanzarExcepcion() {
        when(sesionRepository.findById(1L)).thenReturn(Optional.of(sesionFinalizada));
        when(inscripcionRepository.existsBySesionSesionIdAndUsuarioUsuarioId(1L, 10L)).thenReturn(true);
        when(calificacionRepository.existsBySesionSesionIdAndUsuarioUsuarioId(1L, 10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                calificacionService.calificar(1L, 10L, 3, "")
        );
    }

    // US20: reputacion con calificaciones existentes debe devolver promedio redondeado
    @Test
    void obtenerReputacion_conCalificaciones_debeRetornarPromedio() {
        when(calificacionRepository.calcularReputacionInstructor(5L)).thenReturn(4.333333);

        Double rep = calificacionService.obtenerReputacion(5L);

        assertEquals(4.3, rep);
    }

    // US20: reputacion sin calificaciones debe devolver 0.0
    @Test
    void obtenerReputacion_sinCalificaciones_debeRetornarCero() {
        when(calificacionRepository.calcularReputacionInstructor(5L)).thenReturn(null);

        Double rep = calificacionService.obtenerReputacion(5L);

        assertEquals(0.0, rep);
    }

    // listar calificaciones de una sesion debe delegar al repositorio
    @Test
    void listarPorSesion_debeRetornarLista() {
        Calificacion c = new Calificacion();
        c.setPuntuacion(5);
        when(calificacionRepository.findBySesionSesionId(1L)).thenReturn(List.of(c));

        List<Calificacion> lista = calificacionService.listarPorSesion(1L);

        assertEquals(1, lista.size());
        assertEquals(5, lista.get(0).getPuntuacion());
    }
}
