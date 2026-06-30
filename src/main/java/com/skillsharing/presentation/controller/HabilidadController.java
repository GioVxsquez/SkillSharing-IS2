package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.service.HabilidadService;
import com.skillsharing.domain.entity.Habilidad;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// us21/us22: gestion de habilidades y busqueda de instructores
@RestController
@RequestMapping("/api/habilidades")
@RequiredArgsConstructor
public class HabilidadController {

    private final HabilidadService habilidadService;
    private final UsuarioRepository usuarioRepository;

    // listar todas las habilidades disponibles
    @GetMapping
    public ResponseEntity<ApiResponse<List<Habilidad>>> listar() {
        return ResponseEntity.ok(ApiResponse.exito("habilidades disponibles", habilidadService.listarTodas()));
    }

    // us22: buscar instructores que tengan una habilidad especifica
    @GetMapping("/instructores")
    public ResponseEntity<ApiResponse<List<Usuario>>> buscarInstructores(@RequestParam String habilidad) {
        return ResponseEntity.ok(ApiResponse.exito("instructores encontrados",
                habilidadService.buscarInstructoresPorHabilidad(habilidad)));
    }

    // us21: actualizar mis habilidades en el perfil
    @PutMapping("/mis-habilidades")
    public ResponseEntity<ApiResponse<Usuario>> actualizarMisHabilidades(
            Authentication auth,
            @RequestBody List<Long> habilidadIds) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
        Usuario actualizado = habilidadService.actualizarHabilidades(usuario.getUsuarioId(), habilidadIds);
        return ResponseEntity.ok(ApiResponse.exito("habilidades actualizadas", actualizado));
    }
}
