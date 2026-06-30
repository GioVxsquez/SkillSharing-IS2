package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.service.NotificacionService;
import com.skillsharing.domain.entity.Notificacion;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

// us12/us13: sistema de notificaciones automaticas
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioRepository usuarioRepository;

    // listar todas las notificaciones del usuario autenticado
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notificacion>>> listar(Authentication auth) {
        Long usuarioId = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow().getUsuarioId();
        return ResponseEntity.ok(ApiResponse.exito("notificaciones",
                notificacionService.listarPorUsuario(usuarioId)));
    }

    // contar no vistas (para el badge en la app)
    @GetMapping("/no-vistas/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> contarNoVistas(Authentication auth) {
        Long usuarioId = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow().getUsuarioId();
        long count = notificacionService.contarNoVistas(usuarioId);
        return ResponseEntity.ok(ApiResponse.exito("total no vistas", Map.of("total", count)));
    }

    // marcar una notificacion como vista
    @PutMapping("/{id}/ver")
    public ResponseEntity<ApiResponse<String>> marcarComoVista(@PathVariable Long id) {
        notificacionService.marcarComoVista(id);
        return ResponseEntity.ok(ApiResponse.exito("notificacion marcada como vista", null));
    }

    // marcar todas como vistas
    @PutMapping("/marcar-todas")
    public ResponseEntity<ApiResponse<String>> marcarTodas(Authentication auth) {
        Long usuarioId = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow().getUsuarioId();
        notificacionService.marcarTodasComoVistas(usuarioId);
        return ResponseEntity.ok(ApiResponse.exito("todas las notificaciones marcadas como vistas", null));
    }
}
