package com.skillsharing.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// hu05: material educativo obligatorio para que la sesion pueda ser aprobada
// el instructor debe subir al menos un pdf antes de que el admin apruebe la sesion
// patron adapter (semana 4): la ruta_archivo puede ser local o externa segun el adaptador configurado
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

    // la ruta puede apuntar a un archivo local o a una url externa
    // depende del adaptador de almacenamiento que este configurado
    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    @Column(name = "tipo_archivo", length = 50)
    @Builder.Default
    private String tipoArchivo = "PDF";

    // relacion con la sesion a la que pertenece este material
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionAprendizaje sesion;

    @Column(name = "fecha_subida", nullable = false)
    @Builder.Default
    private LocalDateTime fechaSubida = LocalDateTime.now();
}
