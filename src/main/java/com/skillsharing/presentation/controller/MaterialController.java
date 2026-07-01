package com.skillsharing.presentation.controller;

import com.skillsharing.application.dto.response.ApiResponse;
import com.skillsharing.application.service.MaterialService;
import com.skillsharing.domain.entity.MaterialEducativo;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

// us05: subir materiales educativos
// us27: descargar materiales educativos
@RestController
@RequestMapping("/api/materiales")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final UsuarioRepository usuarioRepository;

    // us05: subir un archivo a una sesion
    @PostMapping("/sesion/{sesionId}")
    public ResponseEntity<ApiResponse<MaterialEducativo>> subir(
            @PathVariable Long sesionId,
            @RequestParam("archivo") MultipartFile archivo,
            Authentication auth) throws Exception {

        Long instructorId = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado")).getUsuarioId();

        MaterialEducativo material = materialService.subirMaterial(sesionId, instructorId, archivo);
        return ResponseEntity.ok(ApiResponse.exito("material subido correctamente", material));
    }

    // listar materiales de una sesion
    @GetMapping("/sesion/{sesionId}")
    public ResponseEntity<ApiResponse<List<MaterialEducativo>>> listar(@PathVariable Long sesionId) {
        return ResponseEntity.ok(ApiResponse.exito("materiales de la sesion",
                materialService.listarPorSesion(sesionId)));
    }

    // us27: descargar un material por su id
    @GetMapping("/{materialId}/descargar")
    public ResponseEntity<Resource> descargar(@PathVariable Long materialId) throws Exception {
        MaterialEducativo material = materialService.obtenerPorId(materialId);
        Resource recurso = materialService.descargarMaterial(materialId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + material.getNombre() + "\"")
                .body(recurso);
    }

    // us05: eliminar un material subido (solo el instructor de la sesion puede hacerlo)
    @DeleteMapping("/{materialId}")
    public ResponseEntity<ApiResponse<String>> eliminar(
            @PathVariable Long materialId,
            Authentication auth) throws Exception {

        Long instructorId = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado")).getUsuarioId();

        materialService.eliminarMaterial(materialId, instructorId);
        return ResponseEntity.ok(ApiResponse.exito("material eliminado correctamente", null));
    }
}

