package com.skillsharing.application.service;

import com.skillsharing.domain.entity.Invitacion;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.EstadoInvitacion;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.infrastructure.repository.InvitacionRepository;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InvitacionServiceHU07Test {

    @Mock
    private InvitacionRepository invitacionRepository;

    @Mock
    private SesionRepository sesionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private InscripcionService inscripcionService;

    @InjectMocks
    private InvitacionService invitacionService;

    private Usuario invitado;
    private Usuario instructor;
    private SesionAprendizaje sesion;
    private Invitacion invitacion;

    @BeforeEach
    void setUp() {

        instructor = Usuario.builder()
                .usuarioId(1L)
                .build();

        invitado = Usuario.builder()
                .usuarioId(2L)
                .build();

        sesion = SesionAprendizaje.builder()
                .sesionId(15L)
                .estado(EstadoSesion.ACTIVA)
                .fechaSesion(LocalDateTime.now().plusDays(3))
                .instructor(instructor)
                .build();

        invitacion = Invitacion.builder()
                .invitacionId(20L)
                .sesion(sesion)
                .invitado(invitado)
                .estado(EstadoInvitacion.PENDIENTE)
                .fechaEnvio(LocalDateTime.now())
                .build();
    }

    @Test
    void aceptarInvitacion_DeberiaCambiarEstadoEInscribirUsuario() {

        when(invitacionRepository.findById(20L))
                .thenReturn(Optional.of(invitacion));

        when(invitacionRepository.save(any(Invitacion.class)))
                .thenAnswer(i -> i.getArgument(0));

        invitacionService.responderInvitacion(
                2L,
                20L,
                true
        );

        assertEquals(
                EstadoInvitacion.ACEPTADA,
                invitacion.getEstado()
        );

        verify(inscripcionService)
                .inscribir(15L,2L);

        verify(invitacionRepository)
                .save(invitacion);
    }

    @Test
    void rechazarInvitacion_DeberiaCambiarEstadoSinInscripcion() {

        when(invitacionRepository.findById(20L))
                .thenReturn(Optional.of(invitacion));

        when(invitacionRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        invitacionService.responderInvitacion(
                2L,
                20L,
                false
        );

        assertEquals(
                EstadoInvitacion.RECHAZADA,
                invitacion.getEstado()
        );

        verify(inscripcionService, never())
                .inscribir(anyLong(), anyLong());

        verify(invitacionRepository)
                .save(invitacion);
    }

}
