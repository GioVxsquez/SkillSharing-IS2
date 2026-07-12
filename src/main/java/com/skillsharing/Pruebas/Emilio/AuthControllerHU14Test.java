package com.skillsharing.Pruebas.Emilio;

import com.skillsharing.application.dto.request.UsuarioRegistroDto;
import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.service.EmailService;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import com.skillsharing.infrastructure.repository.VerificacionTokenRepository;
import com.skillsharing.infrastructure.security.JwtUtil;
import com.skillsharing.presentation.controller.AuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthControllerHU14Test {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    private VerificacionTokenRepository tokenRepository;
    private EmailService emailService;

    private AuthController authController;

    @BeforeEach
    void setUp() {

        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtUtil = mock(JwtUtil.class);
        tokenRepository = mock(VerificacionTokenRepository.class);
        emailService = mock(EmailService.class);

        authController = new AuthController(
                usuarioRepository,
                passwordEncoder,
                authenticationManager,
                jwtUtil,
                tokenRepository,
                emailService
        );
    }

    @Test
    void registroExitoso() {

        UsuarioRegistroDto dto = new UsuarioRegistroDto();
        dto.setNombre("Emilio");
        dto.setEmail("emilio@test.com");
        dto.setPassword("123456");

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("bcrypt");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(i -> i.getArgument(0));

        ResponseEntity<ApiResponse<String>> respuesta =
                authController.registro(dto);

        assertEquals(200, respuesta.getStatusCode().value());
        assertTrue(respuesta.getBody().isOk());

        verify(usuarioRepository).save(any());
    }

    @Test
    void correoYaExiste() {

        UsuarioRegistroDto dto = new UsuarioRegistroDto();
        dto.setNombre("Emilio");
        dto.setEmail("emilio@test.com");
        dto.setPassword("123456");

        when(usuarioRepository.existsByEmail(anyString()))
                .thenReturn(true);

        ResponseEntity<ApiResponse<String>> respuesta =
                authController.registro(dto);

        assertEquals(400, respuesta.getStatusCode().value());
        assertFalse(respuesta.getBody().isOk());

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void errorAlGuardarUsuario() {

        UsuarioRegistroDto dto = new UsuarioRegistroDto();
        dto.setNombre("Emilio");
        dto.setEmail("emilio@test.com");
        dto.setPassword("123456");

        when(usuarioRepository.existsByEmail(anyString()))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("bcrypt");

        when(usuarioRepository.save(any()))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
                () -> authController.registro(dto));
    }

    @Test
    void passwordSeEncriptaAntesDeGuardar() {

        UsuarioRegistroDto dto = new UsuarioRegistroDto();
        dto.setNombre("Emilio");
        dto.setEmail("emilio@test.com");
        dto.setPassword("123456");

        when(usuarioRepository.existsByEmail(anyString()))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("bcrypt");

        when(usuarioRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        authController.registro(dto);

        verify(passwordEncoder).encode("123456");
    }
}