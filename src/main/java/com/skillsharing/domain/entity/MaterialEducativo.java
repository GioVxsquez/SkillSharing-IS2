package com.skillsharing.domain.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
// hu05: material educativo obligatorio para que la sesion pueda ser aprobada
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
    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;
    @Column(name = "tipo_archivo", length = 50)
    private String tipoArchivo = "PDF";
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionAprendizaje sesion;
    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida = LocalDateTime.now();
}
