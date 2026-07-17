package com.skillsharing.Pruebas.Fabrizio;

import com.skillsharing.application.service.CalificacionService;
import com.skillsharing.application.service.HabilidadService;
import com.skillsharing.domain.entity.Habilidad;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.RolUsuario;
import com.skillsharing.infrastructure.repository.CalificacionRepository;
import com.skillsharing.infrastructure.repository.HabilidadRepository;
import com.skillsharing.infrastructure.repository.InscripcionRepository;
import com.skillsharing.infrastructure.repository.SesionRepository;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * PRUEBAS DE CAJA BLANCA
 * -----------------------
 * Se basan en el conocimiento del código fuente: cubrimos cada rama
 * interna (if / else, filtros, casos límite de redondeo) de los servicios
 * que implementan:
 *
 *  - US20: visualizar reputación en el perfil de usuario
 *          -> CalificacionService.obtenerReputacion()
 *  - US21: buscador de instructores por habilidad técnica
 *          -> HabilidadService.buscarInstructoresPorHabilidad()
 */
@ExtendWith(MockitoExtension.class)
class US20_US21_CajaBlancaTest {

    // ---------- US20: reputación ----------
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

    // ---------- US21: buscador por habilidad ----------
    @Mock
    private HabilidadRepository habilidadRepository;

    @InjectMocks
    private HabilidadService habilidadService;

    private Usuario instructorConHabilidad;
    private Usuario instructorSinHabilidad;
    private Usuario aprendizConHabilidad;
    private Habilidad habilidadJava;

    @BeforeEach
    void setUp() {
        habilidadJava = new Habilidad();
        habilidadJava.setHabilidadId(1L);
        habilidadJava.setNombre("Programacion");

        Habilidad habilidadDiseno = new Habilidad();
        habilidadDiseno.setHabilidadId(2L);
        habilidadDiseno.setNombre("Diseno Grafico");

        instructorConHabilidad = new Usuario();
        instructorConHabilidad.setUsuarioId(1L);
        instructorConHabilidad.setNombre("Instructor Java");
        instructorConHabilidad.setRol(RolUsuario.INSTRUCTOR);
        instructorConHabilidad.setHabilidades(new HashSet<>(Set.of(habilidadJava)));

        instructorSinHabilidad = new Usuario();
        instructorSinHabilidad.setUsuarioId(2L);
        instructorSinHabilidad.setNombre("Instructor Diseno");
        instructorSinHabilidad.setRol(RolUsuario.INSTRUCTOR);
        instructorSinHabilidad.setHabilidades(new HashSet<>(Set.of(habilidadDiseno)));

        // rama importante a cubrir: un APRENDIZ puede tener la habilidad
        // pero el metodo NO debe devolverlo porque no es INSTRUCTOR
        aprendizConHabilidad = new Usuario();
        aprendizConHabilidad.setUsuarioId(3L);
        aprendizConHabilidad.setNombre("Aprendiz Java");
        aprendizConHabilidad.setRol(RolUsuario.APRENDIZ);
        aprendizConHabilidad.setHabilidades(new HashSet<>(Set.of(habilidadJava)));
    }

    // ===================== US20: reputación =====================

    @Test
    @DisplayName("US20 - reputación con calificaciones: promedio con decimales se redondea a 1 decimal")
    void obtenerReputacion_promedioConDecimales_debeRedondearAUnDecimal() {
        when(calificacionRepository.calcularReputacionInstructor(1L)).thenReturn(3.666666);

        Double resultado = calificacionService.obtenerReputacion(1L);

        assertEquals(3.7, resultado);
    }

    @Test
    @DisplayName("US20 - reputación con promedio exacto (valor límite superior: 5.0)")
    void obtenerReputacion_promedioMaximo_debeRetornarCincoPuntoCero() {
        when(calificacionRepository.calcularReputacionInstructor(1L)).thenReturn(5.0);

        Double resultado = calificacionService.obtenerReputacion(1L);

        assertEquals(5.0, resultado);
    }

    @Test
    @DisplayName("US20 - reputación con promedio exacto (valor límite inferior: 1.0)")
    void obtenerReputacion_promedioMinimo_debeRetornarUnoPuntoCero() {
        when(calificacionRepository.calcularReputacionInstructor(1L)).thenReturn(1.0);

        Double resultado = calificacionService.obtenerReputacion(1L);

        assertEquals(1.0, resultado);
    }

    @Test
    @DisplayName("US20 - reputación sin calificaciones (rama null) debe retornar 0.0, nunca null")
    void obtenerReputacion_sinCalificaciones_debeRetornarCeroYNoNull() {
        when(calificacionRepository.calcularReputacionInstructor(99L)).thenReturn(null);

        Double resultado = calificacionService.obtenerReputacion(99L);

        assertNotNull(resultado);
        assertEquals(0.0, resultado);
    }

    // ===================== US21: buscador por habilidad =====================

    @Test
    @DisplayName("US21 - buscar instructores: solo retorna INSTRUCTOR, excluye APRENDIZ aunque tenga la habilidad")
    void buscarInstructoresPorHabilidad_debeExcluirAprendices() {
        when(usuarioRepository.findAll())
                .thenReturn(List.of(instructorConHabilidad, aprendizConHabilidad));

        List<Usuario> resultado = habilidadService.buscarInstructoresPorHabilidad("Programacion");

        assertEquals(1, resultado.size());
        assertEquals("Instructor Java", resultado.get(0).getNombre());
    }

    @Test
    @DisplayName("US21 - buscar instructores: la búsqueda es insensible a mayúsculas/minúsculas")
    void buscarInstructoresPorHabilidad_debeSerInsensibleAMayusculas() {
        when(usuarioRepository.findAll()).thenReturn(List.of(instructorConHabilidad));

        List<Usuario> resultado = habilidadService.buscarInstructoresPorHabilidad("PROGRAMACION");

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("US21 - buscar instructores: con varios instructores solo retorna los que calzan con la habilidad")
    void buscarInstructoresPorHabilidad_conVariosInstructores_debeFiltrarCorrectamente() {
        when(usuarioRepository.findAll())
                .thenReturn(List.of(instructorConHabilidad, instructorSinHabilidad));

        List<Usuario> resultado = habilidadService.buscarInstructoresPorHabilidad("Diseno Grafico");

        assertEquals(1, resultado.size());
        assertEquals("Instructor Diseno", resultado.get(0).getNombre());
    }

    @Test
    @DisplayName("US21 - buscar instructores: habilidad inexistente debe retornar lista vacía (no null)")
    void buscarInstructoresPorHabilidad_habilidadInexistente_debeRetornarListaVacia() {
        when(usuarioRepository.findAll()).thenReturn(List.of(instructorConHabilidad));

        List<Usuario> resultado = habilidadService.buscarInstructoresPorHabilidad("Cocina");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
