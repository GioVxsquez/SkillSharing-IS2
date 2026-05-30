package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.request.LoginRequestDto;
import com.skillsharing.application.dto.request.RegistroRequestDto;
import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.service.EmailService;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.entity.VerificacionToken;
import com.skillsharing.domain.enums.RolUsuario;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import com.skillsharing.infrastructure.repository.VerificacionTokenRepository;
import com.skillsharing.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

// controlador de autenticacion: registro, login, verificacion de correo
// HU14 - registrarse
// HU15 - iniciar sesion
// HU29 - activar cuenta por correo
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UsuarioRepository       usuarioRepository;
    private final PasswordEncoder          passwordEncoder;
    private final AuthenticationManager   authenticationManager;
    private final JwtService               jwtService;
    private final EmailService             emailService;
    private final VerificacionTokenRepository tokenRepository;

    // HU14: registrarse - crea la cuenta inactiva y envia correo de verificacion
    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<String>> registro(@RequestBody RegistroRequestDto dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "El email ya está registrado", null));
        }

        RolUsuario rol = dto.getRol() != null ? dto.getRol() : RolUsuario.APRENDIZ;

        // cuenta inactiva hasta que verifique el correo - HU29
        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .rol(rol)
                .activo(false)
                .fechaRegistro(LocalDateTime.now())
                .build();

        usuarioRepository.save(usuario);

        // generar token de verificacion valido por 24 horas
        String tokenStr = UUID.randomUUID().toString();
        VerificacionToken token = VerificacionToken.builder()
                .token(tokenStr)
                .usuario(usuario)
                .fechaExpira(LocalDateTime.now().plusHours(24))
                .usado(false)
                .build();
        tokenRepository.save(token);

        // enviar correo de forma asincrona (no bloquea la respuesta)
        emailService.enviarCorreoVerificacion(usuario.getEmail(), usuario.getNombre(), tokenStr);

        return ResponseEntity.ok(new ApiResponse<>(true,
                "Registro exitoso. Revisa tu correo para activar tu cuenta.", null));
    }

    // HU15: iniciar sesion - devuelve el jwt si las credenciales son correctas
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestBody LoginRequestDto dto) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String jwt = jwtService.generateToken(userDetails);
            return ResponseEntity.ok(new ApiResponse<>(true, "Login exitoso",
                    Map.of("token", jwt)));

        } catch (DisabledException e) {
            return ResponseEntity.status(403).body(new ApiResponse<>(false,
                    "Tu cuenta no está verificada. Revisa tu correo electrónico.", null));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(new ApiResponse<>(false,
                    "Credenciales incorrectas. Verifica tu email y contraseña.", null));
        }
    }

    // HU29: activar cuenta - el usuario hace clic en el enlace del correo
    @GetMapping("/verificar")
    public ResponseEntity<String> verificarCuenta(@RequestParam String token) {
        return tokenRepository.findByToken(token)
                .map(tkn -> {
                    if (tkn.getUsado()) {
                        return ResponseEntity.badRequest()
                                .body(paginaHtml("⚠️ Enlace ya utilizado",
                                        "Este enlace de verificación ya fue usado anteriormente."));
                    }
                    if (tkn.getFechaExpira().isBefore(LocalDateTime.now())) {
                        return ResponseEntity.badRequest()
                                .body(paginaHtml("⏱️ Enlace expirado",
                                        "Este enlace ha expirado. Regístrate nuevamente."));
                    }
                    // activar la cuenta del usuario
                    Usuario usuario = tkn.getUsuario();
                    usuario.setActivo(true);
                    usuarioRepository.save(usuario);

                    // marcar el token como usado
                    tkn.setUsado(true);
                    tokenRepository.save(tkn);

                    log.info("Cuenta verificada exitosamente para: {}", usuario.getEmail());
                    return ResponseEntity.ok(paginaHtml("✅ Cuenta Verificada",
                            "¡Tu cuenta ha sido activada! Ya puedes iniciar sesión en la aplicación SkillSharing."));
                })
                .orElse(ResponseEntity.badRequest()
                        .body(paginaHtml("❌ Token Inválido",
                                "El enlace de verificación no es válido.")));
    }

    // genera una pagina html simple para mostrar al usuario en el navegador
    private String paginaHtml(String titulo, String mensaje) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"><title>%s - SkillSharing</title>
                <style>
                    body{font-family:Arial,sans-serif;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;background:#f0f4ff;}
                    .card{background:#fff;padding:50px;border-radius:16px;text-align:center;box-shadow:0 8px 30px rgba(0,0,0,0.12);max-width:450px;}
                    h1{color:#1B3A6B;font-size:22px;} p{color:#555;font-size:16px;line-height:1.6;}
                    .logo{font-size:28px;font-weight:800;color:#1B3A6B;letter-spacing:2px;margin-bottom:20px;}
                </style>
                </head>
                <body>
                    <div class="card">
                        <div class="logo">SkillSharing</div>
                        <h1>%s</h1>
                        <p>%s</p>
                    </div>
                </body>
                </html>
                """.formatted(titulo, titulo, mensaje);
    }
}
