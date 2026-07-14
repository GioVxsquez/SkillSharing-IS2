package com.skillsharing.application.service;

import com.skillsharing.application.dto.request.SesionRequestDto;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.EstadoSesion;
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

@ExtendWith(MockitoExtension.class)
class SesionServiceTest {

    @Mock
    private SesionRepository sesionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private SesionService sesionService;

    private Usuario instructor;
    private SesionRequestDto dto;

    @BeforeEach
    void setUp() {
        instructor = Usuario.builder()
                .usuarioId(1L)
                .build();

        dto = new SesionRequestDto();
        dto.setTitulo("Introducción a Java");
        dto.setDescripcion("Curso básico de Java");
        dto.setFechaSesion(LocalDateTime.now().plusDays(5));
        dto.setMaxParticipantes(20);
        dto.setModalidad("VIRTUAL");
        dto.setLinkSesion("https://meet.example.com/java");
    }

    @Test
    void deberiaCrearSesionVirtualCorrectamente() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(instructor));

        when(sesionRepository.existsByTituloIgnoreCase(anyString()))
                .thenReturn(false);

        when(sesionRepository
                .countByInstructorUsuarioIdAndEstadoIn(anyLong(), anyList()))
                .thenReturn(0L);

        when(sesionRepository.save(any(SesionAprendizaje.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SesionAprendizaje resultado =
                sesionService.crearSesion(1L, dto);

        assertNotNull(resultado);
        assertEquals("Introducción a Java", resultado.getTitulo());

        verify(sesionRepository, times(1))
                .save(any(SesionAprendizaje.class));
    }

    @Test
    void deberiaCrearSesionPresencialCorrectamente() {
        dto.setModalidad("PRESENCIAL");
        dto.setLugar("Universidad");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(instructor));

        when(sesionRepository.existsByTituloIgnoreCase(anyString()))
                .thenReturn(false);

        when(sesionRepository
                .countByInstructorUsuarioIdAndEstadoIn(anyLong(), anyList()))
                .thenReturn(0L);

        when(sesionRepository.save(any(SesionAprendizaje.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SesionAprendizaje resultado =
                sesionService.crearSesion(1L, dto);

        assertNotNull(resultado);
        assertEquals("Introducción a Java", resultado.getTitulo());

        verify(sesionRepository).save(any());
    }

    @Test
    void noDeberiaCrearSesionConTituloDuplicado() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(instructor));

        when(sesionRepository.existsByTituloIgnoreCase(anyString()))
                .thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> sesionService.crearSesion(1L, dto)
        );

        assertEquals(
                "ya existe una sesion con ese titulo",
                exception.getMessage()
        );

        verify(sesionRepository, never()).save(any());
    }

    @Test
    void noDeberiaCrearMasDeCincoSesionesActivas() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(instructor));

        when(sesionRepository.existsByTituloIgnoreCase(anyString()))
                .thenReturn(false);

        when(sesionRepository
                .countByInstructorUsuarioIdAndEstadoIn(anyLong(), anyList()))
                .thenReturn(5L);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> sesionService.crearSesion(1L, dto)
        );

        assertEquals(
                "alcanzaste el limite de 5 sesiones activas creadas",
                exception.getMessage()
        );

        verify(sesionRepository, never()).save(any());
    }

    @Test
    void noDeberiaCrearSesionConCapacidadInvalida() {
        dto.setMaxParticipantes(101);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(instructor));

        when(sesionRepository.existsByTituloIgnoreCase(anyString()))
                .thenReturn(false);

        when(sesionRepository
                .countByInstructorUsuarioIdAndEstadoIn(anyLong(), anyList()))
                .thenReturn(0L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sesionService.crearSesion(1L, dto)
        );

        assertEquals(
                "la capacidad debe estar entre 1 y 100 participantes",
                exception.getMessage()
        );

        verify(sesionRepository, never()).save(any());
    }

    @Test
    void noDeberiaCrearSesionConModalidadInvalida() {
        dto.setModalidad("HIBRIDA");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(instructor));

        when(sesionRepository.existsByTituloIgnoreCase(anyString()))
                .thenReturn(false);

        when(sesionRepository
                .countByInstructorUsuarioIdAndEstadoIn(anyLong(), anyList()))
                .thenReturn(0L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sesionService.crearSesion(1L, dto)
        );

        assertEquals(
                "modalidad invalida",
                exception.getMessage()
        );

        verify(sesionRepository, never()).save(any());
    }

    @Test
    void deberiaListarSesionesGestionadasPorInstructor() {
        SesionAprendizaje sesion = SesionAprendizaje.builder()
                .sesionId(10L)
                .titulo("Introducción a Java")
                .instructor(instructor)
                .estado(EstadoSesion.ACTIVA)
                .fechaSesion(LocalDateTime.now().plusDays(5))
                .build();

        when(sesionRepository
                .findByInstructorUsuarioIdAndFechaSesionAfterOrderByFechaSesionAsc(
                        eq(1L),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(sesion));

        List<SesionAprendizaje> resultado =
                sesionService.listarPorInstructor(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(
                "Introducción a Java",
                resultado.get(0).getTitulo()
        );
    }

    @Test
    void deberiaRetornarListaVaciaSiInstructorNoTieneSesiones() {
        when(sesionRepository
                .findByInstructorUsuarioIdAndFechaSesionAfterOrderByFechaSesionAsc(
                        eq(1L),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of());

        List<SesionAprendizaje> resultado =
                sesionService.listarPorInstructor(1L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
