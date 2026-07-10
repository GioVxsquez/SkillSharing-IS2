package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.dto.response.ParticipanteResponseDto;
import com.skillsharing.application.dto.response.SesionResponseDto;
import com.skillsharing.application.service.InscripcionService;
import com.skillsharing.domain.entity.Inscripcion;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.InscripcionRepository;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// endpoints de inscripciones para release 1
@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionService inscripcionService;
    private final InscripcionRepository inscripcionRepository;
    private final UsuarioRepository usuarioRepository;

    // hu17: confirmar asistencia publica
    @PostMapping("/{sesionId:[0-9]+}/asistir")
    public ResponseEntity<ApiResponse<String>> asistirPublico(
            @PathVariable Long sesionId,
            Authentication auth) {
            
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
                
        inscripcionService.inscribir(sesionId, usuario.getUsuarioId());
        return ResponseEntity.ok(ApiResponse.exito("asistencia publica confirmada", null));
    }

    // hu10: visualizar eventos asistidos a los que asisto
    @GetMapping("/mis-asistencias")
    public ResponseEntity<ApiResponse<List<SesionResponseDto>>> misAsistencias(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
                
        List<SesionResponseDto> lista = inscripcionService.listarPorUsuario(usuario.getUsuarioId()).stream()
                .map(Inscripcion::getSesion)
                .map(SesionResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.exito("mis asistencias a eventos", lista));
    }

    // hu26: visualizar invitados (asistentes de un evento)
    @GetMapping("/sesion/{sesionId:[0-9]+}/invitados")
    public ResponseEntity<ApiResponse<List<ParticipanteResponseDto>>> verInvitados(
            @PathVariable Long sesionId,
            Authentication auth) {
            
        List<ParticipanteResponseDto> lista = inscripcionRepository.findBySesionSesionId(sesionId).stream()
                .map(ParticipanteResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.exito("lista de invitados confirmados", lista));
    }

    // US18: Desinscribirse de una sesion publica (Nueva)
    @DeleteMapping("/{sesionId}/salir")
    public ResponseEntity<ApiResponse<String>> desinscribirse(
            @PathVariable Long sesionId, Authentication auth) {
        Long usuarioId = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado")).getUsuarioId();
        inscripcionService.desinscribirse(sesionId, usuarioId);
        return ResponseEntity.ok(ApiResponse.exito("Te has retirado de la sesión pública de manera correcta.", null));
    }
}
