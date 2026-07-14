package com.skillsharing.application.service;

import com.skillsharing.domain.entity.Invitacion;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.EstadoInvitacion;
import com.skillsharing.domain.enums.TipoSesion;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitacionServiceTest {

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

    private Usuario organizador;
    private Usuario invitado;
    private SesionAprendizaje sesion;

    @BeforeEach
    void setUp() {

        organizador = Usuario.builder()
                .usuarioId(1L)
                .build();

        invitado = Usuario.builder()
                .usuarioId(2L)
                .build();

        sesion = SesionAprendizaje.builder()
                .sesionId(10L)
                .instructor(organizador)
                .tipo(TipoSesion.PRIVADA)
                .fechaSesion(LocalDateTime.now().plusDays(2))
                .build();
    }

    @Test
    void enviarInvitacion_DeberiaGuardarInvitacion() {

        when(sesionRepository.findById(10L))
                .thenReturn(Optional.of(sesion));

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(invitado));

        when(invitacionRepository.existsBySesionSesionIdAndInvitadoUsuarioId(10L,2L))
                .thenReturn(false);

        when(invitacionRepository.countBySesionSesionIdAndEstadoIn(
                eq(10L),
                anyList()))
                .thenReturn(0L);

        when(invitacionRepository.save(any(Invitacion.class)))
                .thenAnswer(i -> i.getArgument(0));

        Invitacion resultado =
                invitacionService.enviarInvitacion(1L,10L,2L);

        assertNotNull(resultado);
        assertEquals(EstadoInvitacion.PENDIENTE, resultado.getEstado());

        verify(invitacionRepository).save(any(Invitacion.class));
    }
}
