package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.service.InvitacionService;
import com.skillsharing.domain.entity.Invitacion;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        
        Usuario organizador = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
                
        invitacionService.enviarInvitacion(organizador.getUsuarioId(), sesionId, invitadoId);
        return ResponseEntity.ok(ApiResponse.exito("invitacion enviada exitosamente", null));
    }

    // hu28: visualizar invitaciones privadas
    @GetMapping("/mis-invitaciones")
    public ResponseEntity<ApiResponse<List<Invitacion>>> misInvitaciones(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
                
        List<Invitacion> lista = invitacionService.listarMisInvitaciones(usuario.getUsuarioId());
        return ResponseEntity.ok(ApiResponse.exito("invitaciones pendientes", lista));
    }

    // hu07: confirmar asistencia privada
    @PostMapping("/{invitacionId}/responder")
    public ResponseEntity<ApiResponse<String>> responder(
            @PathVariable Long invitacionId, 
            @RequestParam boolean aceptar,
            Authentication auth) {
            
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
                
        invitacionService.responderInvitacion(usuario.getUsuarioId(), invitacionId, aceptar);
        String msj = aceptar ? "asistencia confirmada" : "invitacion rechazada";
        return ResponseEntity.ok(ApiResponse.exito(msj, null));
    }
}
