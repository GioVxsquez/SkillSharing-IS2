package com.skillsharing.application.service;

import com.skillsharing.domain.entity.MaterialEducativo;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.infrastructure.repository.MaterialEducativoRepository;
import com.skillsharing.infrastructure.repository.SesionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

// us05: subir materiales educativos al servidor local
// us27: descargar materiales educativos
// almacenamiento local en el servidor (sin Cloudinary)
@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialService {

    private final MaterialEducativoRepository materialRepository;
    private final SesionRepository sesionRepository;

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    // us05: subir un archivo de material a una sesion
    @Transactional
    public MaterialEducativo subirMaterial(Long sesionId, Long instructorId, MultipartFile archivo) throws IOException {
        SesionAprendizaje sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RuntimeException("sesion no encontrada"));

        if (!sesion.getInstructor().getUsuarioId().equals(instructorId)) {
            throw new SecurityException("solo el instructor de la sesion puede subir materiales");
        }

        // crear directorio si no existe
        Path dirPath = Paths.get(uploadsDir, sesionId.toString());
        Files.createDirectories(dirPath);

        // nombre unico para evitar colisiones
        String nombreOriginal = archivo.getOriginalFilename();
        String extension = nombreOriginal != null && nombreOriginal.contains(".")
                ? nombreOriginal.substring(nombreOriginal.lastIndexOf("."))
                : ".pdf";
        String nombreArchivo = UUID.randomUUID().toString() + extension;
        Path rutaDestino = dirPath.resolve(nombreArchivo);

        Files.copy(archivo.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

        String tipoArchivo = extension.replace(".", "").toUpperCase();
        String rutaRelativa = sesionId + "/" + nombreArchivo;

        MaterialEducativo material = MaterialEducativo.builder()
                .nombre(nombreOriginal != null ? nombreOriginal : nombreArchivo)
                .rutaArchivo(rutaRelativa)
                .tipoArchivo(tipoArchivo)
                .sesion(sesion)
                .build();

        return materialRepository.save(material);
    }

    // us27: obtener el recurso para descarga
    public Resource descargarMaterial(Long materialId) throws MalformedURLException {
        MaterialEducativo material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("material no encontrado"));

        Path rutaArchivo = Paths.get(uploadsDir).resolve(material.getRutaArchivo());
        Resource recurso = new UrlResource(rutaArchivo.toUri());

        if (!recurso.exists() || !recurso.isReadable()) {
            throw new RuntimeException("el archivo no se puede leer o no existe");
        }

        return recurso;
    }

    // listar materiales de una sesion
    public List<MaterialEducativo> listarPorSesion(Long sesionId) {
        return materialRepository.findBySesionSesionId(sesionId);
    }

    // obtener un material por id
    public MaterialEducativo obtenerPorId(Long materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("material no encontrado"));
    }
}
