package com.skillsharing.presentation.controller;
import com.skillsharing.application.dto.request.LoginDto;
import com.skillsharing.application.dto.request.UsuarioRegistroDto;
import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.domain.enums.RolUsuario;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import com.skillsharing.infrastructure.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<String>> registrar(@Valid @RequestBody UsuarioRegistroDto dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("el email ya esta registrado"));
        }
        RolUsuario rol = RolUsuario.APRENDIZ;
        if (dto.getRol() != null && dto.getRol().equalsIgnoreCase("INSTRUCTOR")) {
            rol = RolUsuario.INSTRUCTOR;
        }
        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .rol(rol)
                .activo(false) // hu29: se registra inactivo hasta activar
                .build();
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(ApiResponse.exito("usuario registrado correctamente, revise su correo para activar", null));
    }
    // hu29: activar cuenta
    @PostMapping("/activar")
    public ResponseEntity<ApiResponse<String>> activar(@RequestParam String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(ApiResponse.exito("cuenta activada exitosamente", null));
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@Valid @RequestBody LoginDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(ApiResponse.exito("login exitoso", Map.of("token", token)));
    }
}
