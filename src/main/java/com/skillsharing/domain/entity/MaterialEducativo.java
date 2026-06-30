package com.skillsharing.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// material educativo subido por el instructor a su sesion - us05/us27
// se guarda en el sistema de archivos local del servidor
@Entity
@Table(name = "material_educativo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaterialEducativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Long materialId;

    @Column(nullable = false, length = 200)
    private String nombre;

    // ruta relativa dentro del servidor donde se guarda el archivo
    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    @Column(name = "tipo_archivo", nullable = false, length = 50)
    @Builder.Default
    private String tipoArchivo = "PDF";

    // sesion a la que pertenece este material
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionAprendizaje sesion;

    @Column(name = "fecha_subida", nullable = false)
    @Builder.Default
    private LocalDateTime fechaSubida = LocalDateTime.now();
}
