package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.service.NotificacionService;
import com.skillsharing.domain.entity.Notificacion;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// controlador de notificaciones
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioRepository usuarioRepository;

    // hu09: listar mis notificaciones
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notificacion>>> misNotificaciones(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
        List<Notificacion> lista = notificacionService.listarPorUsuario(usuario.getUsuarioId());
        return ResponseEntity.ok(ApiResponse.exito("mis notificaciones", lista));
    }

    // hu09: listar solo las notificaciones no leidas
    @GetMapping("/no-leidas")
    public ResponseEntity<ApiResponse<List<Notificacion>>> noLeidas(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
        List<Notificacion> lista = notificacionService.listarNoLeidas(usuario.getUsuarioId());
        return ResponseEntity.ok(ApiResponse.exito("notificaciones sin leer", lista));
    }

    // hu09: marcar una notificacion como leida
    @PutMapping("/{id}/leer")
    public ResponseEntity<ApiResponse<String>> marcarLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.ok(ApiResponse.exito("notificacion marcada como leida", null));
    }
}
