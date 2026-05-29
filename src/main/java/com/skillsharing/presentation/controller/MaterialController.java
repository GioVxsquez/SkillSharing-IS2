package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.request.MaterialRequestDto;
import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.service.MaterialService;
import com.skillsharing.domain.entity.MaterialEducativo;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

// controlador para hu05: carga de materiales educativos
@RestController
@RequestMapping("/api/sesiones/{sesionId}/materiales")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> subirMaterial(
            @PathVariable Long sesionId,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("nombre") String nombre,
            Authentication auth) {

        Usuario instructor = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        MaterialRequestDto dto = new MaterialRequestDto();
        dto.setNombre(nombre);
        
        // detecta el tipo por la extension del archivo
        String originalFilename = archivo.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            dto.setTipoArchivo(originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toUpperCase());
        } else {
            dto.setTipoArchivo("DESCONOCIDO");
        }

        try {
            MaterialEducativo material = materialService.subirMaterial(sesionId, instructor.getUsuarioId(), dto, archivo);
            return ResponseEntity.ok(ApiResponse.exito("material subido correctamente", Map.of(
                    "materialId", material.getMaterialId().toString(),
                    "nombre", material.getNombre()
            )));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MaterialEducativo>>> listarMateriales(@PathVariable Long sesionId) {
        // en un sistema real se usa un dto, se omite por simplicidad
        return ResponseEntity.ok(ApiResponse.exito("materiales", materialService.listarPorSesion(sesionId)));
    }
}
