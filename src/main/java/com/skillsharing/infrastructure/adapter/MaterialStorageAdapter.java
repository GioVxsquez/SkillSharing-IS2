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
@Component
public class MaterialStorageAdapter implements MaterialStoragePort {
    @Value("${app.storage.ruta:./uploads}")
    private String rutaBase;
    @Override
    public String guardar(MultipartFile archivo, Long sesionId) {
        try {
            Path directorio = Paths.get(rutaBase, "sesion-" + sesionId);
            Files.createDirectories(directorio);
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
            System.err.println("aviso: no se pudo eliminar el archivo " + rutaArchivo);
        }
    }
}
