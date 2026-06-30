package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.service.CalificacionService;
import com.skillsharing.domain.entity.Calificacion;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

// us19/us20: calificaciones y reputacion del instructor
@RestController
@RequestMapping("/api/calificaciones")
@RequiredArgsConstructor
public class CalificacionController {

    private final CalificacionService calificacionService;
    private final UsuarioRepository usuarioRepository;

    // us19: calificar una sesion finalizada
    @PostMapping("/sesion/{sesionId}")
    public ResponseEntity<ApiResponse<Calificacion>> calificar(
            @PathVariable Long sesionId,
            @RequestBody Map<String, Object> body,
            Authentication auth) {

        Long usuarioId = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow().getUsuarioId();
        Integer puntuacion = (Integer) body.get("puntuacion");
        String comentario = (String) body.getOrDefault("comentario", "");

        Calificacion cal = calificacionService.calificar(sesionId, usuarioId, puntuacion, comentario);
        return ResponseEntity.ok(ApiResponse.exito("calificacion registrada", cal));
    }

    // listar calificaciones de una sesion
    @GetMapping("/sesion/{sesionId}")
    public ResponseEntity<ApiResponse<List<Calificacion>>> listar(@PathVariable Long sesionId) {
        return ResponseEntity.ok(ApiResponse.exito("calificaciones",
                calificacionService.listarPorSesion(sesionId)));
    }

    // us20: reputacion de un instructor
    @GetMapping("/instructor/{instructorId}/reputacion")
    public ResponseEntity<ApiResponse<Map<String, Double>>> reputacion(@PathVariable Long instructorId) {
        Double rep = calificacionService.obtenerReputacion(instructorId);
        return ResponseEntity.ok(ApiResponse.exito("reputacion del instructor", Map.of("promedio", rep)));
    }
}
