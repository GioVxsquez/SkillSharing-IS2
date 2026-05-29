package com.skillsharing.infrastructure.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

// implementacion del patron adapter (semana 4 - patrones estructurales)
// adapta el sistema de archivos local al contrato de MaterialStoragePort
//
// en desarrollo guarda en ./uploads/sesion-{id}/
// en produccion (render) guarda en /opt/render/project/uploads/sesion-{id}/
// la ruta base se configura en application.properties sin cambiar el codigo
//
// principio ocp (semana 2): si se migra a S3, se crea S3StorageAdapter
// sin modificar MaterialService ni MaterialStoragePort
@Component
public class MaterialStorageAdapter implements MaterialStoragePort {

    @Value("${app.storage.ruta:./uploads}")
    private String rutaBase;

    @Override
    public String guardar(MultipartFile archivo, Long sesionId) {
        try {
            // crea el directorio por sesion si no existe
            Path directorio = Paths.get(rutaBase, "sesion-" + sesionId);
            Files.createDirectories(directorio);

            // genera un nombre unico para evitar colisiones entre archivos
            String nombreUnico = UUID.randomUUID() + "-" + archivo.getOriginalFilename();
            Path destino = directorio.resolve(nombreUnico);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            return destino.toString();

        } catch (IOException e) {
            throw new RuntimeException("error al guardar el archivo: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(String rutaArchivo) {
        try {
            Files.deleteIfExists(Paths.get(rutaArchivo));
        } catch (IOException e) {
            // si no se puede borrar el archivo se loguea pero no se interrumpe el flujo
            System.err.println("aviso: no se pudo eliminar el archivo " + rutaArchivo);
        }
    }
}
