package com.skillsharing.Pruebas.Emilio;

import com.skillsharing.application.dto.request.LoginDto;
import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.service.EmailService;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import com.skillsharing.infrastructure.repository.VerificacionTokenRepository;
import com.skillsharing.infrastructure.security.JwtUtil;
import com.skillsharing.presentation.controller.AuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerHU15Test {

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
    void loginExitoso() {

        LoginDto dto = new LoginDto();
        dto.setEmail("emilio@test.com");
        dto.setPassword("123456");

        UserDetails userDetails = new User(
                "emilio@test.com",
                "123456",
                Collections.emptyList()
        );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(jwtUtil.generateToken(userDetails))
                .thenReturn("jwt-token");

        ResponseEntity<ApiResponse<Map<String, String>>> respuesta =
                authController.login(dto);

        assertEquals(200, respuesta.getStatusCode().value());
        assertTrue(respuesta.getBody().isOk());
        assertEquals("jwt-token",
                respuesta.getBody().getData().get("token"));
    }

    @Test
    void credencialesIncorrectas() {

        LoginDto dto = new LoginDto();
        dto.setEmail("emilio@test.com");
        dto.setPassword("123");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException(""));

        ResponseEntity<ApiResponse<Map<String, String>>> respuesta =
                authController.login(dto);

        assertEquals(401, respuesta.getStatusCode().value());
        assertFalse(respuesta.getBody().isOk());
    }

    @Test
    void cuentaInactiva() {

        LoginDto dto = new LoginDto();
        dto.setEmail("emilio@test.com");
        dto.setPassword("123456");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException(""));

        ResponseEntity<ApiResponse<Map<String, String>>> respuesta =
                authController.login(dto);

        assertEquals(403, respuesta.getStatusCode().value());
        assertFalse(respuesta.getBody().isOk());
    }

    @Test
    void generaJwtDespuesDeAutenticar() {

        LoginDto dto = new LoginDto();
        dto.setEmail("emilio@test.com");
        dto.setPassword("123456");

        UserDetails userDetails = new User(
                "emilio@test.com",
                "123456",
                Collections.emptyList()
        );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(jwtUtil.generateToken(any(UserDetails.class)))
                .thenReturn("jwt-token");

        authController.login(dto);

        verify(jwtUtil).generateToken(any(UserDetails.class));
    }
}