package com.skillsharing.application.service;

import com.skillsharing.domain.entity.MaterialEducativo;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.MaterialEducativoRepository;
import com.skillsharing.infrastructure.repository.SesionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// US05 y US27: Subida y listado de materiales educativos
@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialEducativoRepository materialRepository;

    @Mock
    private SesionRepository sesionRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private MaterialService materialService;

    private SesionAprendizaje sesion;
    private Usuario instructor;
    private MaterialEducativo material;

    @BeforeEach
    void setUp() {
        instructor = new Usuario();
        instructor.setUsuarioId(1L);

        sesion = new SesionAprendizaje();
        sesion.setSesionId(5L);
        sesion.setInstructor(instructor);

        material = new MaterialEducativo();
        material.setMaterialId(10L);
        material.setNombre("Clase1.pdf");
        material.setSesion(sesion);
    }

    // US27: Listar materiales de una sesion delega al repo
    @Test
    void listarPorSesion_debeRetornarLista() {
        when(materialRepository.findBySesionSesionIdOrderByFechaSubidaDesc(5L))
                .thenReturn(List.of(material));

        List<MaterialEducativo> resultado = materialService.listarPorSesion(5L);

        assertEquals(1, resultado.size());
        assertEquals("Clase1.pdf", resultado.get(0).getNombre());
    }

    // Eliminar material por el instructor correcto
    @Test
    void eliminarMaterial_comoInstructor_debeEliminarlo() {
        when(materialRepository.findById(10L)).thenReturn(Optional.of(material));
        doNothing().when(materialRepository).delete(material);

        materialService.eliminarMaterial(10L, 1L);

        verify(materialRepository, times(1)).delete(material);
    }

    // Eliminar material sin ser instructor lanza error
    @Test
    void eliminarMaterial_sinPermiso_debeLanzarExcepcion() {
        when(materialRepository.findById(10L)).thenReturn(Optional.of(material));

        assertThrows(SecurityException.class, () ->
                materialService.eliminarMaterial(10L, 99L)
        );

        verify(materialRepository, never()).delete(any());
    }
}
