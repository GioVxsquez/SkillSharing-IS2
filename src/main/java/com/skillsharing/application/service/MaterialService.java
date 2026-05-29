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
@Service
@RequiredArgsConstructor
public class MaterialService {
    private final MaterialRepository materialRepository;
    private final SesionRepository sesionRepository;
    private final MaterialStoragePort storageAdapter; 
    // hu05: subir pdf de ejercicios
    @Transactional
    public MaterialEducativo subirMaterial(Long sesionId, Long instructorId, MaterialRequestDto dto, MultipartFile archivo) {
        SesionAprendizaje sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RuntimeException("sesion no encontrada"));
        if (!sesion.getInstructor().getUsuarioId().equals(instructorId)) {
            throw new SecurityException("solo el instructor puede subir material a esta sesion");
        }
        if (sesion.getEstado() == EstadoSesion.FINALIZADA || sesion.getEstado() == EstadoSesion.RECHAZADA) {
            throw new IllegalStateException("no se puede subir material en este estado de la sesion");
        }
        String rutaArchivo = storageAdapter.guardar(archivo, sesionId);
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
