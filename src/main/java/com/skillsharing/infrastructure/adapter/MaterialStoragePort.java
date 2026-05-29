package com.skillsharing.infrastructure.adapter;

import org.springframework.web.multipart.MultipartFile;

// interfaz del patron adapter (semana 4 - patrones estructurales)
// define el contrato de almacenamiento de archivos, independiente de donde se guarden
//
// principio dip (semana 2): materialservice depende de esta interfaz, no del disco fisico
// principio ocp (semana 2): si se migra a S3 o Cloudinary, solo se crea una nueva clase
// que implemente esta interfaz, sin tocar materialservice
//
// relacion con diagrama de clases:
//   MaterialStoragePort <|-- MaterialStorageAdapter
//   MaterialService --> MaterialStoragePort
public interface MaterialStoragePort {
    // guarda el archivo y retorna la ruta donde quedo almacenado
    String guardar(MultipartFile archivo, Long sesionId);
    // elimina un archivo por su ruta
    void eliminar(String rutaArchivo);
}
