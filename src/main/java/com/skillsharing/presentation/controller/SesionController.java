package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.request.SesionRequestDto;
import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.dto.response.SesionResponseDto;
import com.skillsharing.application.service.BuscadorService;
import com.skillsharing.application.service.SesionService;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// controlador principal para sesiones
@RestController
@RequestMapping("/api/sesiones")
@RequiredArgsConstructor
public class SesionController {

    private final SesionService sesionService;
    private final BuscadorService buscadorService;
    private final UsuarioRepository usuarioRepository;

    // hu06: crear sesion (solo instructores)
    @PostMapping
    public ResponseEntity<ApiResponse<SesionResponseDto>> crearSesion(
            @Valid @RequestBody SesionRequestDto dto,
            Authentication auth) {

        Usuario instructor = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        if (!instructor.getRol().name().equals("INSTRUCTOR")) {
            return ResponseEntity.status(403).body(ApiResponse.error("solo los instructores pueden crear sesiones"));
        }

        SesionAprendizaje creada = sesionService.crearSesion(instructor.getUsuarioId(), dto);
        return ResponseEntity.ok(ApiResponse.exito("sesion creada con exito (pendiente de aprobacion)", SesionResponseDto.fromEntity(creada)));
    }

    // hu07: listar sesiones activas (publico para usuarios autenticados)
    @GetMapping
    public ResponseEntity<ApiResponse<List<SesionResponseDto>>> listarActivas() {
        List<SesionResponseDto> lista = sesionService.listarActivas().stream()
                .map(SesionResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.exito("sesiones activas recuperadas", lista));
    }

    // hu28: buscar sesiones por habilidad o titulo usando el patron strategy
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<SesionResponseDto>>> buscar(
            @RequestParam(defaultValue = "habilidad") String tipo,
            @RequestParam String q) {
            
        List<SesionResponseDto> resultados = buscadorService.buscar(tipo, q).stream()
                .map(SesionResponseDto::fromEntity)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(ApiResponse.exito("resultados de busqueda", resultados));
    }

    // hu08: ver detalle de una sesion especifica
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SesionResponseDto>> verDetalle(@PathVariable Long id) {
        SesionAprendizaje sesion = sesionService.obtenerDetalle(id);
        return ResponseEntity.ok(ApiResponse.exito("detalle de sesion", SesionResponseDto.fromEntity(sesion)));
    }
}
