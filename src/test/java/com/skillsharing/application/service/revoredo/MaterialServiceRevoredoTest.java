package com.skillsharing.application.service.revoredo;

import com.skillsharing.application.service.MaterialService;
import com.skillsharing.domain.entity.MaterialEducativo;
import com.skillsharing.infrastructure.repository.MaterialEducativoRepository;
import com.skillsharing.infrastructure.repository.SesionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceRevoredoTest {

    @Mock
    private MaterialEducativoRepository materialRepository;

    @Mock
    private SesionRepository sesionRepository;

    @InjectMocks
    private MaterialService materialService;

    @TempDir
    Path tempDir;

    // US27: descargar un recurso educativo correctamente
    @Test
    void descargarMaterial_debeRetornarRecursoCorrectamente()
            throws Exception {

        Path carpetaSesion = tempDir.resolve("5");
        Files.createDirectories(carpetaSesion);

        Path archivo = carpetaSesion.resolve("clase1.pdf");
        Files.writeString(
                archivo,
                "contenido de prueba"
        );

        MaterialEducativo material =
                MaterialEducativo.builder()
                        .materialId(10L)
                        .nombre("Clase1.pdf")
                        .rutaArchivo("5/clase1.pdf")
                        .tipoArchivo("PDF")
                        .build();

        when(materialRepository.findById(10L))
                .thenReturn(Optional.of(material));

        ReflectionTestUtils.setField(
                materialService,
                "uploadsDir",
                tempDir.toString()
        );

        Resource resultado =
                materialService.descargarMaterial(10L);

        assertNotNull(resultado);
        assertTrue(resultado.exists());
        assertTrue(resultado.isReadable());
        assertEquals(
                "clase1.pdf",
                resultado.getFilename()
        );

        verify(
                materialRepository,
                times(1)
        ).findById(10L);
    }

    // US27: no descargar un material inexistente
    @Test
    void descargarMaterial_materialInexistente_debeLanzarExcepcion() {

        when(materialRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> materialService.descargarMaterial(99L)
        );

        assertEquals(
                "material no encontrado",
                exception.getMessage()
        );
    }

    // US27: no descargar si el archivo fisico no existe
    @Test
    void descargarMaterial_archivoInexistente_debeLanzarExcepcion()
            throws Exception {

        MaterialEducativo material =
                MaterialEducativo.builder()
                        .materialId(10L)
                        .nombre("Clase1.pdf")
                        .rutaArchivo("5/inexistente.pdf")
                        .tipoArchivo("PDF")
                        .build();

        when(materialRepository.findById(10L))
                .thenReturn(Optional.of(material));

        ReflectionTestUtils.setField(
                materialService,
                "uploadsDir",
                tempDir.toString()
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> materialService.descargarMaterial(10L)
        );

        assertEquals(
                "el archivo no se puede leer o no existe",
                exception.getMessage()
        );
    }
}
