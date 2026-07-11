package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.request.SesionRequestDto;
import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.dto.response.ParticipanteResponseDto;
import com.skillsharing.application.dto.response.SesionResponseDto;
import com.skillsharing.application.service.InscripcionService;
import com.skillsharing.application.service.SesionService;
import com.skillsharing.domain.entity.Inscripcion;
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
    private final InscripcionService inscripcionService;
    private final UsuarioRepository usuarioRepository;

    // hu01: crear sesion (solo instructores)
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
        return ResponseEntity.ok(ApiResponse.exito("sesion creada con exito", SesionResponseDto.fromEntity(creada)));
    }

    // hu07: listar sesiones activas (publico para usuarios autenticados)
    @GetMapping
    public ResponseEntity<ApiResponse<List<SesionResponseDto>>> listarActivas() {
        List<SesionResponseDto> lista = sesionService.listarActivas().stream()
                .map(SesionResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.exito("sesiones activas recuperadas", lista));
    }

    // hu16: listar sesiones publicas para la pantalla principal del aplicativo
    @GetMapping("/publicas")
    public ResponseEntity<ApiResponse<List<SesionResponseDto>>> listarPublicas() {
        return listarActivas();
    }

    // hu02: alias usado por la pantalla mis sesiones
    @GetMapping("/mis-sesiones")
    public ResponseEntity<ApiResponse<List<SesionResponseDto>>> misSesiones(Authentication auth) {
        return misEventos(auth);
    }

    // hu10: sesiones donde participa el usuario autenticado
    @GetMapping("/mis-inscripciones")
    public ResponseEntity<ApiResponse<List<SesionResponseDto>>> misInscripciones(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        List<SesionResponseDto> lista = inscripcionService.listarPorUsuario(usuario.getUsuarioId()).stream()
                .map(Inscripcion::getSesion)
                .map(SesionResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.exito("sesiones a las que asisto", lista));
    }

    // hu17: alias usado por detalle de sesion para confirmar asistencia publica
    @PostMapping("/{sesionId:[0-9]+}/inscribirse")
    public ResponseEntity<ApiResponse<String>> inscribirse(
            @PathVariable Long sesionId,
            Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        inscripcionService.inscribir(sesionId, usuario.getUsuarioId());
        return ResponseEntity.ok(ApiResponse.exito("asistencia publica confirmada", null));
    }

    // hu26: asistentes confirmados de una sesion
    @GetMapping("/{sesionId:[0-9]+}/invitados")
    public ResponseEntity<ApiResponse<List<ParticipanteResponseDto>>> invitados(@PathVariable Long sesionId) {
        List<ParticipanteResponseDto> lista = inscripcionService.listarInvitados(sesionId).stream()
                .map(ParticipanteResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.exito("invitados confirmados", lista));
    }

    // hu04: ver detalle de una sesion especifica
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ApiResponse<SesionResponseDto>> verDetalle(@PathVariable Long id) {
        SesionAprendizaje sesion = sesionService.obtenerDetalle(id);
        return ResponseEntity.ok(ApiResponse.exito("detalle de sesion", SesionResponseDto.fromEntity(sesion)));
    }

    // hu02: visualizar eventos gestionados
    @GetMapping("/mis-eventos")
    public ResponseEntity<ApiResponse<List<SesionResponseDto>>> misEventos(Authentication auth) {
        Usuario instructor = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        List<SesionResponseDto> lista = sesionService.listarPorInstructor(instructor.getUsuarioId()).stream()
                .map(SesionResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.exito("mis eventos gestionados", lista));
    }

    // US11: filtrar por modalidad
    @GetMapping("/modalidad/{modalidad}")
    public ResponseEntity<ApiResponse<List<SesionResponseDto>>> porModalidad(@PathVariable String modalidad) {
        List<SesionResponseDto> lista = sesionService.filtrarPorModalidad(modalidad).stream()
                .map(SesionResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.exito("sesiones por modalidad", lista));
    }
}
