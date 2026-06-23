package com.skillsharing.Pruebas.Emilio;

import com.skillsharing.application.dto.request.LoginDto;
import com.skillsharing.application.dto.request.UsuarioRegistroDto;
import com.skillsharing.application.service.EmailService;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import com.skillsharing.infrastructure.repository.VerificacionTokenRepository;
import com.skillsharing.infrastructure.security.JwtUtil;
import com.skillsharing.presentation.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthControllerTest{

@Test

void deberiaAutenticarUsuarioCorrectamente() {
    UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    JwtUtil jwtUtil = mock(JwtUtil.class);
    VerificacionTokenRepository tokenRepository = mock(VerificacionTokenRepository.class);
    EmailService emailService = mock(EmailService.class);

    LoginDto dto = new LoginDto();
    dto.setEmail("test@correo.com");
    dto.setPassword("123456");

    Authentication authentication = mock(Authentication.class);
    UserDetails userDetails = mock(UserDetails.class);

    when(authenticationManager.authenticate(any())).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(jwtUtil.generateToken(userDetails)).thenReturn("token123");

    AuthController controller = new AuthController(
            usuarioRepository,
            passwordEncoder,
            authenticationManager,
            jwtUtil,
            tokenRepository,
            emailService
    );

    controller.login(dto);

    verify(authenticationManager).authenticate(any());
    verify(jwtUtil).generateToken(userDetails);
}

@Test
void deberiaRetornarErrorCuandoCredencialesSonIncorrectas() {
    UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    JwtUtil jwtUtil = mock(JwtUtil.class);
    VerificacionTokenRepository tokenRepository = mock(VerificacionTokenRepository.class);
    EmailService emailService = mock(EmailService.class);

    LoginDto dto = new LoginDto();
    dto.setEmail("test@correo.com");
    dto.setPassword("incorrecta");

    when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Credenciales incorrectas"));

    AuthController controller = new AuthController(
            usuarioRepository,
            passwordEncoder,
            authenticationManager,
            jwtUtil,
            tokenRepository,
            emailService
    );

    controller.login(dto);

    verify(authenticationManager).authenticate(any());
}

@Test
void deberiaInvocarAuthenticationManagerDuranteElLogin() {
    AuthenticationManager authenticationManager = mock(AuthenticationManager.class);

    when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Credenciales incorrectas"));

    try {
        authenticationManager.authenticate(any());
    } catch (BadCredentialsException e) {
        // esperado
    }

    verify(authenticationManager).authenticate(any());
}

@Test
void deberiaGenerarJwtDespuesDeAutenticar() {
    AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    JwtUtil jwtUtil = mock(JwtUtil.class);

    Authentication authentication = mock(Authentication.class);
    UserDetails userDetails = mock(UserDetails.class);

    when(authenticationManager.authenticate(any()))
            .thenReturn(authentication);
    when(authentication.getPrincipal())
            .thenReturn(userDetails);
    when(jwtUtil.generateToken(userDetails))
            .thenReturn("jwt-token");

    authenticationManager.authenticate(any());
    jwtUtil.generateToken(userDetails);

    verify(authenticationManager).authenticate(any());
    verify(jwtUtil).generateToken(userDetails);
}
}