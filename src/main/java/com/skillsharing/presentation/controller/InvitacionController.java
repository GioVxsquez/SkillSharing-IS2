package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.dto.response.InvitacionResponseDto;
import com.skillsharing.application.service.InvitacionService;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// endpoints de invitaciones para release 1
@RestController
@RequestMapping("/api/invitaciones")
@RequiredArgsConstructor
public class InvitacionController {

    private final InvitacionService invitacionService;
    private final UsuarioRepository usuarioRepository;

    // hu06: invitar asistentes
    @PostMapping("/sesion/{sesionId}/invitar/{invitadoId}")
    public ResponseEntity<ApiResponse<String>> invitar(
            @PathVariable Long sesionId, 
            @PathVariable Long invitadoId,
            Authentication auth) {
        return enviar(sesionId, invitadoId, auth);
    }

    // hu06: alias usado por la pantalla de invitar asistentes
    @PostMapping
    public ResponseEntity<ApiResponse<String>> invitarConBody(
            @RequestBody Map<String, Long> body,
            Authentication auth) {
        Long sesionId = body.get("sesionId");
        Long invitadoId = body.getOrDefault("invitadoId", body.get("receptorId"));
        return enviar(sesionId, invitadoId, auth);
    }

    private ResponseEntity<ApiResponse<String>> enviar(Long sesionId, Long invitadoId, Authentication auth) {
        Usuario organizador = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        invitacionService.enviarInvitacion(organizador.getUsuarioId(), sesionId, invitadoId);
        return ResponseEntity.ok(ApiResponse.exito("invitacion enviada exitosamente", null));
    }

    // hu28: visualizar invitaciones privadas
    @GetMapping("/mis-invitaciones")
    public ResponseEntity<ApiResponse<List<InvitacionResponseDto>>> misInvitaciones(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
                
        List<InvitacionResponseDto> lista = invitacionService.listarMisInvitaciones(usuario.getUsuarioId()).stream()
                .map(InvitacionResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.exito("invitaciones pendientes", lista));
    }

    // hu07: confirmar asistencia privada
    @PostMapping("/{invitacionId}/responder")
    public ResponseEntity<ApiResponse<String>> responder(
            @PathVariable Long invitacionId, 
            @RequestParam boolean aceptar,
            Authentication auth) {
        return responderInterno(invitacionId, aceptar, auth);
    }

    // hu07: alias usado por la app movil
    @PutMapping("/{invitacionId}/responder")
    public ResponseEntity<ApiResponse<String>> responderPut(
            @PathVariable Long invitacionId,
            @RequestParam boolean aceptar,
            Authentication auth) {
        return responderInterno(invitacionId, aceptar, auth);
    }

    private ResponseEntity<ApiResponse<String>> responderInterno(Long invitacionId, boolean aceptar, Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        invitacionService.responderInvitacion(usuario.getUsuarioId(), invitacionId, aceptar);
        String msj = aceptar ? "asistencia confirmada" : "invitacion rechazada";
        return ResponseEntity.ok(ApiResponse.exito(msj, null));
    }
}
