package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.dto.response.InstructorResponseDto;
import com.skillsharing.application.service.CalificacionService;
import com.skillsharing.application.service.HabilidadService;
import com.skillsharing.domain.entity.Habilidad;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

// us21/us22: gestion de habilidades y busqueda de instructores
@RestController
@RequestMapping("/api/habilidades")
@RequiredArgsConstructor
public class HabilidadController {

    private final HabilidadService habilidadService;
    private final CalificacionService calificacionService;
    private final UsuarioRepository usuarioRepository;

    // listar todas las habilidades disponibles
    @GetMapping
    public ResponseEntity<ApiResponse<List<Habilidad>>> listar() {
        return ResponseEntity.ok(ApiResponse.exito("habilidades disponibles", habilidadService.listarTodas()));
    }

    // us22: buscar instructores que tengan una habilidad especifica
    // devuelve InstructorResponseDto con habilidades y reputacion (us20)
    @GetMapping("/instructores")
    public ResponseEntity<ApiResponse<List<InstructorResponseDto>>> buscarInstructores(@RequestParam String habilidad) {
        List<InstructorResponseDto> resultado = habilidadService.buscarInstructoresPorHabilidad(habilidad)
                .stream()
                .map(u -> {
                    Double rep = calificacionService.obtenerReputacion(u.getUsuarioId());
                    return InstructorResponseDto.fromEntity(u, rep);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.exito("instructores encontrados", resultado));
    }

    // us21: actualizar mis habilidades en el perfil
    @PutMapping("/mis-habilidades")
    public ResponseEntity<ApiResponse<String>> actualizarMisHabilidades(
            Authentication auth,
            @RequestBody List<Long> habilidadIds) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
        habilidadService.actualizarHabilidades(usuario.getUsuarioId(), habilidadIds);
        return ResponseEntity.ok(ApiResponse.exito("habilidades actualizadas correctamente", null));
    }
}
