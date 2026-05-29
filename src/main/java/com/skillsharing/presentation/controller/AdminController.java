package com.skillsharing.presentation.controller;
import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.dto.response.SesionResponseDto;
import com.skillsharing.application.facade.SesionFacade;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.enums.EstadoSesion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// hu10: controlador exclusivo de administracion
@RestController
@RequestMapping("/api/admin/sesiones")
@RequiredArgsConstructor
public class AdminController {
    private final SesionFacade sesionFacade;
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<ApiResponse<SesionResponseDto>> aprobarSesion(@PathVariable Long id) {
        try {
            SesionAprendizaje aprobada = sesionFacade.cambiarEstado(id, EstadoSesion.ACTIVA);
            return ResponseEntity.ok(ApiResponse.exito("sesion aprobada con exito", SesionResponseDto.fromEntity(aprobada)));
        } catch (IllegalStateException e) {
            // ocurre si intenta aprobar sin material educativo cargado (hu05)
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<ApiResponse<SesionResponseDto>> rechazarSesion(@PathVariable Long id) {
        try {
            SesionAprendizaje rechazada = sesionFacade.cambiarEstado(id, EstadoSesion.RECHAZADA);
            return ResponseEntity.ok(ApiResponse.exito("sesion rechazada", SesionResponseDto.fromEntity(rechazada)));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
