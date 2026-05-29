package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.service.HabilidadService;
import com.skillsharing.domain.entity.Habilidad;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// controlador de habilidades
@RestController
@RequestMapping("/api/habilidades")
@RequiredArgsConstructor
public class HabilidadController {

    private final HabilidadService habilidadService;

    // hu22: listar todas las habilidades disponibles para el perfil
    @GetMapping
    public ResponseEntity<ApiResponse<List<Habilidad>>> listar() {
        List<Habilidad> habilidades = habilidadService.listarTodas();
        return ResponseEntity.ok(ApiResponse.exito("lista de habilidades disponibles", habilidades));
    }
}
