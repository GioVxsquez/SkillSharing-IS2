package com.skillsharing.Pruebas.Fabrizio;

import com.skillsharing.application.dto.response.InstructorResponseDto;
import com.skillsharing.application.service.CalificacionService;
import com.skillsharing.application.service.HabilidadService;
import com.skillsharing.domain.entity.Habilidad;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.RolUsuario;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import com.skillsharing.infrastructure.security.JwtAuthFilter;
import com.skillsharing.infrastructure.security.JwtUtil;
import com.skillsharing.infrastructure.security.UserDetailsServiceImpl;
import com.skillsharing.presentation.controller.CalificacionController;
import com.skillsharing.presentation.controller.HabilidadController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PRUEBAS DE CAJA NEGRA
 * ----------------------
 * No nos importa cómo está implementado el código por dentro: solo
 * probamos el contrato del API (entradas -> salidas esperadas), usando
 * clases de equivalencia (dato válido / inválido / vacío) y valores límite.
 *
 *  - US20: GET /api/calificaciones/instructor/{id}/reputacion
 *  - US21: GET /api/habilidades/instructores?habilidad=X
 *
 * Se usa @WebMvcTest para levantar solo la capa web (controladores) sin
 * necesitar una base de datos real, y se desactivan los filtros de
 * seguridad (JWT) porque la autenticación no es parte de estas dos US.
 */
@WebMvcTest(controllers = {CalificacionController.class, HabilidadController.class})
@AutoConfigureMockMvc(addFilters = false)
class US20_US21_CajaNegraTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalificacionService calificacionService;

    @MockBean
    private HabilidadService habilidadService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    // estos 3 beans no son parte de la lógica de US20/US21, pero @WebMvcTest
    // los intenta cargar igual porque JwtAuthFilter es de tipo Filter.
    // Los mockeamos para que el contexto de test levante sin necesitar
    // una configuración de seguridad real.
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    // ===================== US20: reputación =====================

    @Test
    @DisplayName("US20 - GET reputacion con id valido y calificaciones -> 200 y promedio correcto")
    void reputacion_idValidoConCalificaciones_debeRetornar200ConPromedio() throws Exception {
        when(calificacionService.obtenerReputacion(1L)).thenReturn(4.5);

        mockMvc.perform(get("/api/calificaciones/instructor/1/reputacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.promedio").value(4.5));
    }

    @Test
    @DisplayName("US20 - GET reputacion con id valido pero sin calificaciones -> 200 y promedio 0.0")
    void reputacion_idValidoSinCalificaciones_debeRetornarPromedioCero() throws Exception {
        when(calificacionService.obtenerReputacion(2L)).thenReturn(0.0);

        mockMvc.perform(get("/api/calificaciones/instructor/2/reputacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.promedio").value(0.0));
    }

    @Test
    @DisplayName("US20 - GET reputacion con id NO numerico (clase inválida) -> 400 Bad Request")
    void reputacion_idNoNumerico_debeRetornar400() throws Exception {
        mockMvc.perform(get("/api/calificaciones/instructor/abc/reputacion"))
                .andExpect(status().isBadRequest());
    }

    // ===================== US21: buscador por habilidad =====================

    @Test
    @DisplayName("US21 - GET buscar instructores con habilidad existente -> 200 y lista con instructores")
    void buscarInstructores_habilidadExistente_debeRetornar200ConInstructores() throws Exception {
        Habilidad java = new Habilidad();
        java.setHabilidadId(1L);
        java.setNombre("Java");

        Usuario instructor = new Usuario();
        instructor.setUsuarioId(1L);
        instructor.setNombre("Instructor Java");
        instructor.setEmail("instructor@test.com");
        instructor.setRol(RolUsuario.INSTRUCTOR);
        instructor.setHabilidades(new HashSet<>(Set.of(java)));

        when(habilidadService.buscarInstructoresPorHabilidad("Java"))
                .thenReturn(List.of(instructor));
        when(calificacionService.obtenerReputacion(1L)).thenReturn(4.2);

        mockMvc.perform(get("/api/habilidades/instructores").param("habilidad", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].nombre").value("Instructor Java"))
                .andExpect(jsonPath("$.data[0].reputacionPromedio").value(4.2));
    }

    @Test
    @DisplayName("US21 - GET buscar instructores con habilidad inexistente -> 200 y lista vacía")
    void buscarInstructores_habilidadInexistente_debeRetornarListaVacia() throws Exception {
        when(habilidadService.buscarInstructoresPorHabilidad("Cocina"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/habilidades/instructores").param("habilidad", "Cocina"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("US21 - GET buscar instructores SIN el parámetro obligatorio 'habilidad' -> 400 Bad Request")
    void buscarInstructores_sinParametroHabilidad_debeRetornar400() throws Exception {
        mockMvc.perform(get("/api/habilidades/instructores"))
                .andExpect(status().isBadRequest());
    }
}
