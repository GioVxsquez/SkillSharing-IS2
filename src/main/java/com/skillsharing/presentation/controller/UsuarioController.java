package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.dto.response.UsuarioResponseDto;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioResponseDto>> obtenerMiPerfil(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
                
        return ResponseEntity.ok(ApiResponse.exito("perfil de usuario", UsuarioResponseDto.fromEntity(usuario)));
    }

    // hu06: buscar usuarios para invitarlos a sesiones privadas
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<UsuarioResponseDto>>> buscarUsuarios(@RequestParam String q) {
        List<UsuarioResponseDto> usuarios = usuarioRepository
                .findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q)
                .stream()
                .map(UsuarioResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.exito("usuarios encontrados", usuarios));
    }
}
