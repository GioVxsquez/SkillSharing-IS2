package com.skillsharing.infrastructure.adapter;
import org.springframework.web.multipart.MultipartFile;
public interface MaterialStoragePort {
    String guardar(MultipartFile archivo, Long sesionId);
    void eliminar(String rutaArchivo);
}
