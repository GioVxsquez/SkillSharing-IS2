package com.skillsharing.Pruebas.Emilio;

import com.skillsharing.application.dto.request.UsuarioRegistroDto;
import com.skillsharing.application.service.EmailService;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import com.skillsharing.infrastructure.repository.VerificacionTokenRepository;
import com.skillsharing.infrastructure.security.JwtUtil;
import com.skillsharing.presentation.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthControllerTest {
@Test
void deberiaVerificarSiEmailExiste() {
    UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    JwtUtil jwtUtil = mock(JwtUtil.class);
    VerificacionTokenRepository tokenRepository = mock(VerificacionTokenRepository.class);
    EmailService emailService = mock(EmailService.class);

    UsuarioRegistroDto dto = new UsuarioRegistroDto();
    dto.setEmail("test@correo.com");

    when(usuarioRepository.existsByEmail(dto.getEmail())).thenReturn(true);

    AuthController controller = new AuthController(
            usuarioRepository,
            passwordEncoder,
            authenticationManager,
            jwtUtil,
            tokenRepository,
            emailService
    );

    controller.registro(dto);

    verify(usuarioRepository).existsByEmail(dto.getEmail());
}

@Test
void deberiaGuardarUsuarioCuandoEmailNoExiste() {
    UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    JwtUtil jwtUtil = mock(JwtUtil.class);
    VerificacionTokenRepository tokenRepository = mock(VerificacionTokenRepository.class);
    EmailService emailService = mock(EmailService.class);

    UsuarioRegistroDto dto = new UsuarioRegistroDto();
    dto.setNombre("Emilio");
    dto.setEmail("test@correo.com");
    dto.setPassword("123456");

    when(usuarioRepository.existsByEmail(dto.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(any())).thenReturn("clave");
    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

    AuthController controller = new AuthController(
            usuarioRepository,
            passwordEncoder,
            authenticationManager,
            jwtUtil,
            tokenRepository,
            emailService
    );

    controller.registro(dto);

    verify(usuarioRepository).save(any(Usuario.class));
}
}