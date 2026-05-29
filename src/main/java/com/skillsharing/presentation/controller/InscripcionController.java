package com.skillsharing.presentation.controller;
import com.skillsharing.application.dto.response.ApiResponse;
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
@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {
    private final InscripcionService inscripcionService;
    private final InscripcionRepository inscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    // hu17: confirmar asistencia publica
    @PostMapping("/{sesionId}/asistir")
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
    public ResponseEntity<ApiResponse<List<Inscripcion>>> misAsistencias(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
        List<Inscripcion> lista = inscripcionService.listarPorUsuario(usuario.getUsuarioId());
        return ResponseEntity.ok(ApiResponse.exito("mis asistencias a eventos", lista));
    }
    // hu26: visualizar invitados (asistentes de un evento)
    @GetMapping("/sesion/{sesionId}/invitados")
    public ResponseEntity<ApiResponse<List<Inscripcion>>> verInvitados(
            @PathVariable Long sesionId,
            Authentication auth) {
        List<Inscripcion> lista = inscripcionRepository.findBySesionSesionId(sesionId);
        return ResponseEntity.ok(ApiResponse.exito("lista de invitados confirmados", lista));
    }
}
