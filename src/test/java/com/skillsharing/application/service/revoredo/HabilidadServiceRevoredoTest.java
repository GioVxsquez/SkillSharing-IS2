package com.skillsharing.application.service.revoredo;

import com.skillsharing.application.service.HabilidadService;
import com.skillsharing.domain.entity.Habilidad;
import com.skillsharing.domain.entity.Usuario;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabilidadServiceRevoredoTest {

    @Mock
    private HabilidadRepository habilidadRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private HabilidadService habilidadService;

    private Usuario usuario;
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

        usuario = new Usuario();
        usuario.setUsuarioId(1L);
        usuario.setHabilidades(
                new HashSet<>(Set.of(habilidadJava))
        );
    }

    // US22: reemplazar las habilidades anteriores
    @Test
    void actualizarHabilidades_debeReemplazarLasAnteriores() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(habilidadRepository.findById(2L))
                .thenReturn(Optional.of(habilidadDiseno));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuario);

        Usuario resultado =
                habilidadService.actualizarHabilidades(
                        1L,
                        List.of(2L)
                );

        assertNotNull(resultado);
        assertEquals(1, resultado.getHabilidades().size());
        assertTrue(
                resultado.getHabilidades().contains(habilidadDiseno)
        );
        assertFalse(
                resultado.getHabilidades().contains(habilidadJava)
        );

        verify(usuarioRepository, times(1)).save(usuario);
    }

    // US22: agregar varias habilidades
    @Test
    void actualizarHabilidades_debePermitirVariasHabilidades() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(habilidadRepository.findById(1L))
                .thenReturn(Optional.of(habilidadJava));

        when(habilidadRepository.findById(2L))
                .thenReturn(Optional.of(habilidadDiseno));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuario);

        Usuario resultado =
                habilidadService.actualizarHabilidades(
                        1L,
                        List.of(1L, 2L)
                );

        assertEquals(2, resultado.getHabilidades().size());
        assertTrue(
                resultado.getHabilidades().contains(habilidadJava)
        );
        assertTrue(
                resultado.getHabilidades().contains(habilidadDiseno)
        );
    }

    // US22: usuario inexistente
    @Test
    void actualizarHabilidades_usuarioInexistente_debeLanzarExcepcion() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> habilidadService.actualizarHabilidades(
                        99L,
                        List.of(1L)
                )
        );

        verify(usuarioRepository, never()).save(any());
    }

    // US22: habilidad inexistente
    @Test
    void actualizarHabilidades_habilidadInexistente_debeLanzarExcepcion() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(habilidadRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> habilidadService.actualizarHabilidades(
                        1L,
                        List.of(99L)
                )
        );

        verify(usuarioRepository, never()).save(any());
    }

    // US22: eliminar todas las habilidades
    @Test
    void actualizarHabilidades_debePermitirVaciarCatalogo() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuario);

        Usuario resultado =
                habilidadService.actualizarHabilidades(
                        1L,
                        List.of()
                );

        assertNotNull(resultado);
        assertTrue(resultado.getHabilidades().isEmpty());

        verify(usuarioRepository, times(1)).save(usuario);
    }
}
