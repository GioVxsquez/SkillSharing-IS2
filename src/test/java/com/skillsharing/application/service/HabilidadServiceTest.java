package com.skillsharing.application.service;

import com.skillsharing.domain.entity.Habilidad;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.RolUsuario;
import com.skillsharing.infrastructure.repository.HabilidadRepository;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// pruebas unitarias del HabilidadService - US21 y US22
@ExtendWith(MockitoExtension.class)
class HabilidadServiceTest {

    @Mock
    private HabilidadRepository habilidadRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private HabilidadService habilidadService;

    private Usuario instructor;
    private Habilidad habilidadJava;
    private Habilidad habilidadDiseno;

    @BeforeEach
    void setUp() {
        habilidadJava = new Habilidad();
        habilidadJava.setHabilidadId(1L);
        habilidadJava.setNombre("Programacion");

        habilidadDiseno = new Habilidad();
        habilidadDiseno.setHabilidadId(2L);
        habilidadDiseno.setNombre("Diseno Grafico");

        instructor = new Usuario();
        instructor.setUsuarioId(1L);
        instructor.setNombre("Instructor");
        instructor.setRol(RolUsuario.INSTRUCTOR);
        instructor.setHabilidades(new HashSet<>(Set.of(habilidadJava)));
    }

    // US21: actualizar habilidades del usuario debe reemplazar las anteriores
    @Test
    void actualizarHabilidades_debeReemplazarLasAnteriores() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(habilidadRepository.findById(2L)).thenReturn(Optional.of(habilidadDiseno));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(instructor);

        Usuario resultado = habilidadService.actualizarHabilidades(1L, List.of(2L));

        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).save(instructor);
        // habilidades del instructor ahora debe contener solo Diseno Grafico
        assertTrue(instructor.getHabilidades().contains(habilidadDiseno));
        assertFalse(instructor.getHabilidades().contains(habilidadJava));
    }

    // US21: habilidad no encontrada durante actualizacion debe lanzar excepcion
    @Test
    void actualizarHabilidades_habilidadInexistente_debeLanzarExcepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(habilidadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                habilidadService.actualizarHabilidades(1L, List.of(99L))
        );
    }

    // US22: buscar instructores por habilidad debe retornar solo instructores con esa habilidad
    @Test
    void buscarInstructoresPorHabilidad_debeRetornarSoloInstructoresConLaHabilidad() {
        Usuario aprendiz = new Usuario();
        aprendiz.setRol(RolUsuario.APRENDIZ);
        aprendiz.setHabilidades(new HashSet<>(Set.of(habilidadJava)));

        when(usuarioRepository.findAll()).thenReturn(List.of(instructor, aprendiz));

        List<Usuario> resultado = habilidadService.buscarInstructoresPorHabilidad("Programacion");

        assertEquals(1, resultado.size());
        assertEquals("Instructor", resultado.get(0).getNombre());
    }

    // US22: habilidad sin instructores debe retornar lista vacia
    @Test
    void buscarInstructoresPorHabilidad_sinResultados_debeRetornarVacio() {
        when(usuarioRepository.findAll()).thenReturn(List.of(instructor));

        List<Usuario> resultado = habilidadService.buscarInstructoresPorHabilidad("Cocina");

        assertTrue(resultado.isEmpty());
    }

    // listar todas las habilidades debe delegar al repositorio
    @Test
    void listarTodas_debeRetornarListaCompleta() {
        when(habilidadRepository.findAll()).thenReturn(List.of(habilidadJava, habilidadDiseno));

        List<Habilidad> habilidades = habilidadService.listarTodas();

        assertEquals(2, habilidades.size());
    }
}
