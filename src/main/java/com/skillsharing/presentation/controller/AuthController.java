package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.request.LoginDto;
import com.skillsharing.application.dto.request.UsuarioRegistroDto;
import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.service.EmailService;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.entity.VerificacionToken;
import com.skillsharing.domain.enums.RolUsuario;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import com.skillsharing.infrastructure.repository.VerificacionTokenRepository;
import com.skillsharing.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

// controlador de autenticacion: registro, login y verificacion
// HU14 - registrarse
// HU15 - iniciar sesion
// HU29 - activar cuenta
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final VerificacionTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${app.email.verification-enabled:true}")
    private boolean verificacionCorreoActiva;

    @Value("${app.verification.token.expiry-ms:1800000}")
    private long tokenExpiryMs;

    // HU14: registrarse - crea la cuenta y prepara la verificacion por correo
    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<String>> registro(@RequestBody UsuarioRegistroDto dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "El email ya esta registrado", null));
        }

        RolUsuario rol = (dto.getRol() != null && !dto.getRol().trim().isEmpty())
                ? RolUsuario.valueOf(dto.getRol().toUpperCase())
                : RolUsuario.APRENDIZ;

        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .rol(rol)
                .activo(!verificacionCorreoActiva)
                .fechaRegistro(LocalDateTime.now())
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        if (verificacionCorreoActiva) {
            String token = UUID.randomUUID().toString();
            VerificacionToken verificacionToken = VerificacionToken.builder()
                    .token(token)
                    .usuario(usuarioGuardado)
                    .fechaExpira(LocalDateTime.now().plus(Duration.ofMillis(tokenExpiryMs)))
                    .usado(false)
                    .build();
            tokenRepository.save(verificacionToken);
            emailService.enviarCorreoVerificacion(usuarioGuardado.getEmail(), usuarioGuardado.getNombre(), token);

            return ResponseEntity.ok(new ApiResponse<>(true,
                    "Registro exitoso. Revisa tu correo para activar la cuenta.", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(true,
                "Registro exitoso. Ya puedes iniciar sesion.", null));
    }

    // HU15: iniciar sesion - devuelve el jwt si las credenciales son correctas
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestBody LoginDto dto) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String jwt = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new ApiResponse<>(true, "Login exitoso",
                    Map.of("token", jwt)));

        } catch (DisabledException e) {
            return ResponseEntity.status(403).body(new ApiResponse<>(false,
                    "Tu cuenta no esta activa. Revisa el correo de verificacion.", null));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(new ApiResponse<>(false,
                    "Credenciales incorrectas. Verifica tu email y contrasena.", null));
        }
    }

    // HU29: activar cuenta - se conserva por trazabilidad del release
    @GetMapping("/verificar")
    public ResponseEntity<String> verificarCuenta(@RequestParam String token) {
        return tokenRepository.findByToken(token)
                .map(tkn -> {
                    if (tkn.getUsado()) {
                        return ResponseEntity.badRequest()
                                .body(paginaHtml("Enlace ya utilizado",
                                        "Este enlace de verificacion ya fue usado anteriormente."));
                    }
                    if (tkn.getFechaExpira().isBefore(LocalDateTime.now())) {
                        return ResponseEntity.badRequest()
                                .body(paginaHtml("Enlace expirado",
                                        "Este enlace ha expirado. Registrate nuevamente."));
                    }

                    Usuario usuario = tkn.getUsuario();
                    usuario.setActivo(true);
                    usuarioRepository.save(usuario);

                    tkn.setUsado(true);
                    tokenRepository.save(tkn);

                    log.info("Cuenta verificada exitosamente para: {}", usuario.getEmail());
                    return ResponseEntity.ok(paginaHtml("Cuenta verificada",
                            "Tu cuenta ha sido activada. Ya puedes iniciar sesion en la aplicacion SkillSharing."));
                })
                .orElse(ResponseEntity.badRequest()
                        .body(paginaHtml("Token invalido",
                                "El enlace de verificacion no es valido.")));
    }

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
