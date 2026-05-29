package com.skillsharing.application.service;

import com.skillsharing.application.dto.request.MaterialRequestDto;
import com.skillsharing.domain.entity.MaterialEducativo;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.infrastructure.adapter.MaterialStoragePort;
import com.skillsharing.infrastructure.repository.MaterialRepository;
import com.skillsharing.infrastructure.repository.SesionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

// servicio para hu05: carga de material educativo obligatorio
//
// principio dip (semana 2): depende de MaterialStoragePort (interfaz adapter)
// polimorfismo (poo semana 1): usa la interfaz adapter sin saber como se guarda fisicamente
@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final SesionRepository sesionRepository;
    private final MaterialStoragePort storageAdapter; // inyeccion del patron adapter

    // hu05: subir pdf de ejercicios
    // cuando se sube el primer material, se actualiza material_cargado = true
    // lo que permite al admin aprobar la sesion
    @Transactional
    public MaterialEducativo subirMaterial(Long sesionId, Long instructorId, MaterialRequestDto dto, MultipartFile archivo) {
        SesionAprendizaje sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RuntimeException("sesion no encontrada"));

        // validacion: solo el instructor creador puede subir material
        if (!sesion.getInstructor().getUsuarioId().equals(instructorId)) {
            throw new SecurityException("solo el instructor puede subir material a esta sesion");
        }

        // no se puede subir si ya esta finalizada o rechazada
        if (sesion.getEstado() == EstadoSesion.FINALIZADA || sesion.getEstado() == EstadoSesion.RECHAZADA) {
            throw new IllegalStateException("no se puede subir material en este estado de la sesion");
        }

        // usa el patron adapter para guardar el archivo fisico
        String rutaArchivo = storageAdapter.guardar(archivo, sesionId);

        // crea y guarda el registro en base de datos
        MaterialEducativo material = MaterialEducativo.builder()
                .nombre(dto.getNombre())
                .rutaArchivo(rutaArchivo)
                .tipoArchivo(dto.getTipoArchivo())
                .sesion(sesion)
                .build();
        
        MaterialEducativo guardado = materialRepository.save(material);

        // hu05 crucial: actualiza el flag en la sesion para permitir aprobacion
        if (!sesion.getMaterialCargado()) {
            sesion.setMaterialCargado(true);
            sesionRepository.save(sesion);
        }

        return guardado;
    }

    public List<MaterialEducativo> listarPorSesion(Long sesionId) {
        return materialRepository.findBySesionSesionId(sesionId);
    }
}
